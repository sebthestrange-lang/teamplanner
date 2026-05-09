package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.dto.MitarbeiterFilterDTO;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Team;
import de.teamplanner.repository.MitarbeiterRepository;
import de.teamplanner.specification.MitarbeiterSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MitarbeiterService {

    private static final Logger log = LoggerFactory.getLogger(MitarbeiterService.class);

    private final MitarbeiterRepository mitarbeiterRepository;
    private final OrgContext orgContext;

    private Specification<Mitarbeiter> byOrg() {
        Long orgId = orgContext.getOrgId();
        return (root, query, cb) -> cb.equal(root.get("organisation").get("id"), orgId);
    }

    public List<Mitarbeiter> alleMitarbeiter() {
        return mitarbeiterRepository.findAll(byOrg());
    }

    public List<Mitarbeiter> mitFilter(MitarbeiterFilterDTO filter) {
        return mitarbeiterRepository.findAll(byOrg().and(MitarbeiterSpecification.withFilter(filter)));
    }

    public List<Mitarbeiter> findByTeam(Team team) {
        return mitarbeiterRepository.findByTeamOrderByNachnameAsc(team);
    }

    public Optional<Mitarbeiter> findById(Long id) {
        return mitarbeiterRepository.findByIdAndOrganisationId(id, orgContext.getOrgId());
    }

    public Mitarbeiter findByIdOrThrow(Long id) {
        return mitarbeiterRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Mitarbeiter", id));
    }

    @Transactional
    public Mitarbeiter speichern(Mitarbeiter mitarbeiter) {
        if (mitarbeiter.getId() == null) {
            mitarbeiter.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Mitarbeiter: {} {}", mitarbeiter.getVorname(), mitarbeiter.getNachname());
        return mitarbeiterRepository.save(mitarbeiter);
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Mitarbeiter mit ID {}", id);
        mitarbeiterRepository.deleteById(id);
    }

    public List<Mitarbeiter> findOhneTeam() {
        return mitarbeiterRepository.findByOrganisationIdAndTeamIsNullOrderByNachnameAsc(orgContext.getOrgId());
    }

    @Transactional
    public void zuTeamZuweisen(Long mitarbeiterId, Team team) {
        Mitarbeiter mitarbeiter = findByIdOrThrow(mitarbeiterId);
        mitarbeiter.setTeam(team);
        mitarbeiterRepository.save(mitarbeiter);
        log.debug("Mitarbeiter {} dem Team {} zugewiesen", mitarbeiter.getVollstaendigerName(), team.getName());
    }

    @Transactional
    public void ausTeamEntfernen(Long mitarbeiterId) {
        Mitarbeiter mitarbeiter = findByIdOrThrow(mitarbeiterId);
        mitarbeiter.setTeam(null);
        mitarbeiterRepository.save(mitarbeiter);
        log.debug("Mitarbeiter {} aus Team entfernt", mitarbeiter.getVollstaendigerName());
    }

    public long anzahl() {
        return mitarbeiterRepository.countByOrganisationId(orgContext.getOrgId());
    }
}
