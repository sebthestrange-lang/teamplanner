package de.teamplaner.repository;

import de.teamplaner.model.Mitarbeiter;
import de.teamplaner.model.Notiz;
import de.teamplaner.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotizRepository extends JpaRepository<Notiz, Long> {

    List<Notiz> findByTeamOrderByErstelltAmDesc(Team team);

    List<Notiz> findByMitarbeiterOrderByErstelltAmDesc(Mitarbeiter mitarbeiter);
}
