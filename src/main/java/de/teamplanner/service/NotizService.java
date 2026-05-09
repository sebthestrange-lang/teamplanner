package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Notiz;
import de.teamplanner.model.Team;
import de.teamplanner.repository.NotizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotizService {

    private final NotizRepository notizRepository;
    private final OrgContext orgContext;
    private final AuditService auditService;

    public List<Notiz> findByTeam(Team team) {
        return notizRepository.findByTeamOrderByErstelltAmDesc(team);
    }

    public List<Notiz> findByMitarbeiter(Mitarbeiter mitarbeiter) {
        return notizRepository.findByMitarbeiterOrderByErstelltAmDesc(mitarbeiter);
    }

    @Transactional
    public void hinzufuegenFuerTeam(String inhalt, Team team) {
        Notiz notiz = notizRepository.save(Notiz.builder()
                .inhalt(inhalt)
                .team(team)
                .organisation(orgContext.getOrganisation())
                .build());
        auditService.log("Notiz", notiz.getId(), "CREATE");
    }

    @Transactional
    public void hinzufuegenFuerMitarbeiter(String inhalt, Mitarbeiter mitarbeiter) {
        Notiz notiz = notizRepository.save(Notiz.builder()
                .inhalt(inhalt)
                .mitarbeiter(mitarbeiter)
                .organisation(orgContext.getOrganisation())
                .build());
        auditService.log("Notiz", notiz.getId(), "CREATE");
    }

    @Transactional
    public void loeschen(Long id) {
        if (!notizRepository.existsById(id)) {
            throw new EntityNotFoundException("Notiz", id);
        }
        notizRepository.deleteById(id);
        auditService.log("Notiz", id, "DELETE");
    }
}
