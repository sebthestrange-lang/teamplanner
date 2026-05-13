package de.teamplaner.service;

import de.teamplaner.config.OrgContext;
import de.teamplaner.exception.EntityNotFoundException;
import de.teamplaner.model.Mitarbeiter;
import de.teamplaner.model.Notiz;
import de.teamplaner.model.Team;
import de.teamplaner.repository.NotizRepository;
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
