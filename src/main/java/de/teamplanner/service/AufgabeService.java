package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.dto.AufgabeFilterDTO;
import de.teamplanner.dto.TeamAuslastungDTO;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Aufgabe;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Team;
import de.teamplanner.model.enums.AufgabenStatus;
import de.teamplanner.repository.AufgabeRepository;
import de.teamplanner.specification.AufgabeSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AufgabeService {

    private static final Logger log = LoggerFactory.getLogger(AufgabeService.class);

    private final AufgabeRepository aufgabeRepository;
    private final OrgContext orgContext;

    private Specification<Aufgabe> byOrg() {
        Long orgId = orgContext.getOrgId();
        return (root, query, cb) -> cb.equal(root.get("organisation").get("id"), orgId);
    }

    public List<Aufgabe> alleAufgaben() {
        return aufgabeRepository.findAll(byOrg());
    }

    public List<Aufgabe> mitFilter(AufgabeFilterDTO filter) {
        return aufgabeRepository.findAll(byOrg().and(AufgabeSpecification.withFilter(filter)));
    }

    public List<Aufgabe> findByProjektId(Long projektId) {
        return aufgabeRepository.findByProjektId(projektId);
    }

    public List<Aufgabe> findByMitarbeiter(Mitarbeiter mitarbeiter) {
        return aufgabeRepository.findByMitarbeiter(mitarbeiter);
    }

    public Optional<Aufgabe> findById(Long id) {
        return aufgabeRepository.findByIdAndOrganisationId(id, orgContext.getOrgId());
    }

    public Aufgabe findByIdOrThrow(Long id) {
        return aufgabeRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Aufgabe", id));
    }

    @Transactional
    public Aufgabe speichern(Aufgabe aufgabe) {
        if (aufgabe.getId() == null) {
            aufgabe.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Aufgabe: {}", aufgabe.getTitel());
        return aufgabeRepository.save(aufgabe);
    }

    @Transactional
    public Aufgabe statusAendern(Long id, AufgabenStatus neuerStatus) {
        Aufgabe aufgabe = findByIdOrThrow(id);
        return statusAendern(aufgabe, neuerStatus);
    }

    @Transactional
    public Aufgabe statusAendern(Aufgabe aufgabe, AufgabenStatus neuerStatus) {
        aufgabe.setStatus(neuerStatus);
        if (neuerStatus == AufgabenStatus.ABGESCHLOSSEN && aufgabe.getAbgeschlossenAm() == null) {
            aufgabe.setAbgeschlossenAm(LocalDateTime.now());
        } else if (neuerStatus != AufgabenStatus.ABGESCHLOSSEN) {
            aufgabe.setAbgeschlossenAm(null);
        }
        return aufgabeRepository.save(aufgabe);
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Aufgabe mit ID {}", id);
        aufgabeRepository.deleteById(id);
    }

    public boolean isUeberfaellig(Aufgabe aufgabe) {
        return aufgabe.getFaelligAm() != null
                && aufgabe.getFaelligAm().isBefore(LocalDate.now())
                && aufgabe.getStatus() != AufgabenStatus.ABGESCHLOSSEN;
    }

    public List<Aufgabe> heuteFaellig() {
        Specification<Aufgabe> spec = byOrg().and(
                (root, query, cb) -> cb.equal(root.get("faelligAm"), LocalDate.now()));
        return aufgabeRepository.findAll(spec);
    }

    public List<Aufgabe> letzteAktivitaeten(int anzahl) {
        return aufgabeRepository.findAll(byOrg(),
                PageRequest.of(0, anzahl, Sort.by(Sort.Direction.DESC, "erstelltAm"))).getContent();
    }

    public long anzahlOffen() {
        Long orgId = orgContext.getOrgId();
        return aufgabeRepository.countByOrganisationIdAndStatus(orgId, AufgabenStatus.OFFEN)
             + aufgabeRepository.countByOrganisationIdAndStatus(orgId, AufgabenStatus.IN_BEARBEITUNG);
    }

    public long anzahlUeberfaellig() {
        return aufgabeRepository.countByOrganisationIdAndUeberfaellig(
                orgContext.getOrgId(), LocalDate.now(), AufgabenStatus.ABGESCHLOSSEN);
    }

    public long anzahlNachStatus(AufgabenStatus status) {
        return aufgabeRepository.countByOrganisationIdAndStatus(orgContext.getOrgId(), status);
    }

    public TeamAuslastungDTO auslastungFuerTeam(Team team) {
        long offen         = aufgabeRepository.countByTeamAndStatus(team, AufgabenStatus.OFFEN);
        long inBearbeitung = aufgabeRepository.countByTeamAndStatus(team, AufgabenStatus.IN_BEARBEITUNG);
        long abgeschlossen = aufgabeRepository.countByTeamAndStatus(team, AufgabenStatus.ABGESCHLOSSEN);
        return new TeamAuslastungDTO(offen, inBearbeitung, abgeschlossen);
    }
}
