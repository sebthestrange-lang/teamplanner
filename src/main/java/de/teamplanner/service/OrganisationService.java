package de.teamplanner.service;

import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Organisation;
import de.teamplanner.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    public List<Organisation> alle() {
        return organisationRepository.findAllByOrderByNameAsc();
    }

    public Organisation findByIdOrThrow(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation", id));
    }

    @Transactional
    public Organisation speichern(Organisation organisation) {
        return organisationRepository.save(organisation);
    }

    @Transactional
    public void loeschen(Long id) {
        organisationRepository.deleteById(id);
    }
}
