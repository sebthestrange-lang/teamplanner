package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Meeting;
import de.teamplanner.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final OrgContext orgContext;

    public List<Meeting> alle() {
        return meetingRepository.findByOrganisationIdOrderByDatumDesc(orgContext.getOrgId());
    }

    public Meeting findByIdOrThrow(Long id) {
        return meetingRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Meeting", id));
    }

    @Transactional
    public Meeting speichern(Meeting meeting) {
        if (meeting.getId() == null) {
            meeting.setOrganisation(orgContext.getOrganisation());
        }
        return meetingRepository.save(meeting);
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        meetingRepository.deleteById(id);
    }
}
