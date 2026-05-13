package de.teamplaner.repository;

import de.teamplaner.model.Projekt;
import de.teamplaner.model.Team;
import de.teamplaner.model.enums.ProjektStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjektRepository extends JpaRepository<Projekt, Long>,
        JpaSpecificationExecutor<Projekt> {

    List<Projekt> findByTeam(Team team);

    List<Projekt> findByStatus(ProjektStatus status);

    List<Projekt> findByTeamAndStatus(Team team, ProjektStatus status);

    long countByStatus(ProjektStatus status);

    List<Projekt> findByOrganisationIdOrderByNameAsc(Long organisationId);

    Optional<Projekt> findByIdAndOrganisationId(Long id, Long organisationId);
}
