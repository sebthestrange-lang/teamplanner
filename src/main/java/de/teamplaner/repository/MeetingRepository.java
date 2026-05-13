package de.teamplaner.repository;

import de.teamplaner.model.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByOrganisationIdOrderByDatumDesc(Long organisationId);

    Optional<Meeting> findByIdAndOrganisationId(Long id, Long organisationId);
}
