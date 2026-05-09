package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Team;
import de.teamplanner.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final Logger log = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final OrgContext orgContext;
    private final AuditService auditService;

    public List<Team> alleTeams() {
        return teamRepository.findByOrganisationIdOrderByNameAsc(orgContext.getOrgId());
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findByIdAndOrganisationId(id, orgContext.getOrgId());
    }

    public Team findByIdOrThrow(Long id) {
        return teamRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Team", id));
    }

    @Transactional
    public Team speichern(Team team) {
        boolean isNeu = team.getId() == null;
        if (isNeu) {
            team.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Team: {}", team.getName());
        Team gespeichert = teamRepository.save(team);
        auditService.log("Team", gespeichert.getId(), isNeu ? "CREATE" : "UPDATE");
        return gespeichert;
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Team mit ID {}", id);
        teamRepository.deleteById(id);
        auditService.log("Team", id, "DELETE");
    }

    public long anzahl() {
        return teamRepository.countByOrganisationId(orgContext.getOrgId());
    }
}
