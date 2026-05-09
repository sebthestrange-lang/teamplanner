package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.dto.ProjektFilterDTO;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Aufgabe;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Projekt;
import de.teamplanner.model.Team;
import de.teamplanner.model.enums.AufgabenStatus;
import de.teamplanner.repository.AufgabeRepository;
import de.teamplanner.repository.ProjektRepository;
import de.teamplanner.specification.ProjektSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjektService {

    private static final Logger log = LoggerFactory.getLogger(ProjektService.class);

    private final ProjektRepository projektRepository;
    private final AufgabeRepository aufgabeRepository;
    private final OrgContext orgContext;
    private final AuditService auditService;

    private Specification<Projekt> byOrg() {
        Long orgId = orgContext.getOrgId();
        return (root, query, cb) -> cb.equal(root.get("organisation").get("id"), orgId);
    }

    public List<Projekt> alleProjekte() {
        return projektRepository.findAll(byOrg());
    }

    public List<Projekt> mitFilter(ProjektFilterDTO filter) {
        return projektRepository.findAll(byOrg().and(ProjektSpecification.withFilter(filter)));
    }

    public List<Projekt> findByTeam(Team team) {
        return projektRepository.findByTeam(team);
    }

    public Optional<Projekt> findById(Long id) {
        return projektRepository.findByIdAndOrganisationId(id, orgContext.getOrgId());
    }

    public Projekt findByIdOrThrow(Long id) {
        return projektRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Projekt", id));
    }

    @Transactional
    public Projekt speichern(Projekt projekt) {
        boolean isNeu = projekt.getId() == null;
        if (isNeu) {
            projekt.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Projekt: {}", projekt.getName());
        Projekt gespeichert = projektRepository.save(projekt);
        auditService.log("Projekt", gespeichert.getId(), isNeu ? "CREATE" : "UPDATE");
        return gespeichert;
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Projekt mit ID {}", id);
        projektRepository.deleteById(id);
        auditService.log("Projekt", id, "DELETE");
    }

    public List<Mitarbeiter> getBeteiligteMitarbeiter(Long projektId) {
        return aufgabeRepository.findByProjektId(projektId)
                .stream()
                .map(Aufgabe::getMitarbeiter)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    public Map<Long, Map<String, Long>> getAufgabenStatistikJeMitarbeiter(Long projektId) {
        List<Aufgabe> aufgaben = aufgabeRepository.findByProjektId(projektId);
        Map<Long, Map<String, Long>> statistik = new HashMap<>();
        for (Aufgabe aufgabe : aufgaben) {
            if (aufgabe.getMitarbeiter() == null) continue;
            Long mitarbeiterId = aufgabe.getMitarbeiter().getId();
            statistik.putIfAbsent(mitarbeiterId, new HashMap<>(Map.of("offen", 0L, "erledigt", 0L)));
            if (aufgabe.getStatus() == AufgabenStatus.ABGESCHLOSSEN) {
                statistik.get(mitarbeiterId).merge("erledigt", 1L, Long::sum);
            } else {
                statistik.get(mitarbeiterId).merge("offen", 1L, Long::sum);
            }
        }
        return statistik;
    }

    public long anzahl() {
        return projektRepository.countByStatus(de.teamplanner.model.enums.ProjektStatus.AKTIV);
    }
}
