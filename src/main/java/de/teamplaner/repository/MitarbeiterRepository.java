package de.teamplaner.repository;

import de.teamplaner.model.Mitarbeiter;
import de.teamplaner.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MitarbeiterRepository extends JpaRepository<Mitarbeiter, Long>,
        JpaSpecificationExecutor<Mitarbeiter> {

    List<Mitarbeiter> findByTeam(Team team);

    List<Mitarbeiter> findByTeamOrderByNachnameAsc(Team team);

    long countByTeam(Team team);

    List<Mitarbeiter> findByTeamIsNullOrTeamNotOrderByNachnameAsc(Team team);

    List<Mitarbeiter> findByTeamIsNullOrderByNachnameAsc();

    List<Mitarbeiter> findByOrganisationIdAndTeamIsNullOrderByNachnameAsc(Long organisationId);

    long countByOrganisationId(Long organisationId);

    Optional<Mitarbeiter> findByIdAndOrganisationId(Long id, Long organisationId);
}
