package de.teamplanner.repository;

import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Notiz;
import de.teamplanner.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotizRepository extends JpaRepository<Notiz, Long> {

    List<Notiz> findByTeamOrderByErstelltAmDesc(Team team);

    List<Notiz> findByMitarbeiterOrderByErstelltAmDesc(Mitarbeiter mitarbeiter);
}
