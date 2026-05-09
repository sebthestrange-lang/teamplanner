package de.teamplanner.repository;

import de.teamplanner.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    List<Organisation> findAllByOrderByNameAsc();
}
