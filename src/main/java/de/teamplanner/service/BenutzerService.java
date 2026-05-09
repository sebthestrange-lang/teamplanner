package de.teamplanner.service;

import de.teamplanner.dto.BenutzerFormDTO;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Benutzer;
import de.teamplanner.model.Organisation;
import de.teamplanner.model.enums.Rolle;
import de.teamplanner.repository.BenutzerRepository;
import de.teamplanner.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BenutzerService {

    private static final Logger log = LoggerFactory.getLogger(BenutzerService.class);

    private final BenutzerRepository benutzerRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;

    public List<Benutzer> findAll() {
        return benutzerRepository.findAll();
    }

    public Benutzer findByIdOrThrow(Long id) {
        return benutzerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Benutzer", id));
    }

    @Transactional
    public Benutzer speichern(BenutzerFormDTO dto, Benutzer existing) {
        Benutzer benutzer = existing != null ? existing : new Benutzer();
        if (existing == null) {
            benutzer.setBenutzername(dto.getBenutzername());
            Organisation org = organisationRepository.findById(dto.getOrganisationId())
                    .orElseThrow(() -> new EntityNotFoundException("Organisation", dto.getOrganisationId()));
            benutzer.setOrganisation(org);
        }
        benutzer.setAnzeigename(dto.getAnzeigename());

        if (existing != null) {
            String aktuellerName = SecurityContextHolder.getContext().getAuthentication().getName();
            boolean isSelf = existing.getBenutzername().equals(aktuellerName);
            if (isSelf && !dto.isAktiv()) {
                throw new IllegalStateException("Eigener Account kann nicht deaktiviert werden.");
            }
            if (isSelf && dto.getRolle() != Rolle.ADMIN) {
                throw new IllegalStateException("Eigene Admin-Rolle kann nicht geändert werden.");
            }
        }

        benutzer.setRolle(dto.getRolle());
        benutzer.setAktiv(dto.isAktiv());
        if (StringUtils.hasText(dto.getPasswort())) {
            benutzer.setPasswort(passwordEncoder.encode(dto.getPasswort()));
        }
        log.debug("Speichere Benutzer: {}", benutzer.getBenutzername());
        return benutzerRepository.save(benutzer);
    }

    @Transactional
    public void loeschen(Long id) {
        Benutzer benutzer = findByIdOrThrow(id);
        String aktuellerName = SecurityContextHolder.getContext().getAuthentication().getName();
        if (benutzer.getBenutzername().equals(aktuellerName)) {
            throw new IllegalStateException("Eigener Account kann nicht gelöscht werden.");
        }
        log.debug("Lösche Benutzer: {}", benutzer.getBenutzername());
        benutzerRepository.deleteById(id);
    }

    public long count() {
        return benutzerRepository.count();
    }
}
