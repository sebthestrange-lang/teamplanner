package de.teamplaner.repository;

import de.teamplaner.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrganisationRepository extends JpaRepository<Organisation, Long> {

    List<Organisation> findAllByOrderByNameAsc();
}
