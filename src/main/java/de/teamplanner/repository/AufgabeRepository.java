package de.teamplanner.repository;

import de.teamplanner.model.Aufgabe;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Projekt;
import de.teamplanner.model.Team;
import de.teamplanner.model.enums.AufgabenStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AufgabeRepository extends JpaRepository<Aufgabe, Long>,
        JpaSpecificationExecutor<Aufgabe> {

    List<Aufgabe> findByMitarbeiter(Mitarbeiter mitarbeiter);

    List<Aufgabe> findByProjekt(Projekt projekt);

    List<Aufgabe> findByStatus(AufgabenStatus status);

    List<Aufgabe> findByFaelligAmBefore(LocalDate datum);

    List<Aufgabe> findByFaelligAm(LocalDate datum);

    List<Aufgabe> findByProjektId(Long projektId);

    long countByStatus(AufgabenStatus status);

    long countByFaelligAmBeforeAndStatusNot(LocalDate datum, AufgabenStatus status);

    @Query("SELECT a FROM Aufgabe a ORDER BY a.erstelltAm DESC")
    List<Aufgabe> findLatest(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(a) FROM Aufgabe a WHERE a.mitarbeiter.team = :team AND a.status = :status")
    long countByTeamAndStatus(@Param("team") Team team, @Param("status") AufgabenStatus status);

    @Query("SELECT COUNT(a) FROM Aufgabe a WHERE a.organisation.id = :orgId AND a.status = :status")
    long countByOrganisationIdAndStatus(@Param("orgId") Long orgId, @Param("status") AufgabenStatus status);

    @Query("SELECT COUNT(a) FROM Aufgabe a WHERE a.organisation.id = :orgId AND a.faelligAm < :datum AND a.status != :status")
    long countByOrganisationIdAndUeberfaellig(@Param("orgId") Long orgId, @Param("datum") LocalDate datum, @Param("status") AufgabenStatus status);

    Optional<Aufgabe> findByIdAndOrganisationId(Long id, Long organisationId);
}
