package de.teamplaner.service;

import de.teamplaner.exception.EntityNotFoundException;
import de.teamplaner.model.Organisation;
import de.teamplaner.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final AuditService auditService;

    public List<Organisation> alle() {
        return organisationRepository.findAllByOrderByNameAsc();
    }

    public Organisation findByIdOrThrow(Long id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Organisation", id));
    }

    @Transactional
    public Organisation speichern(Organisation organisation) {
        boolean isNeu = organisation.getId() == null;
        Organisation gespeichert = organisationRepository.save(organisation);
        auditService.log("Organisation", gespeichert.getId(), isNeu ? "CREATE" : "UPDATE");
        return gespeichert;
    }

    @Transactional
    public void loeschen(Long id) {
        organisationRepository.deleteById(id);
        auditService.log("Organisation", id, "DELETE");
    }
}
