package de.teamplanner.repository;

import de.teamplanner.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);

    List<Team> findByOrganisationIdOrderByNameAsc(Long organisationId);

    Optional<Team> findByIdAndOrganisationId(Long id, Long organisationId);

    long countByOrganisationId(Long organisationId);
}
