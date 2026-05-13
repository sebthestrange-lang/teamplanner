package de.teamplaner.config;

import de.teamplaner.model.Benutzer;
import de.teamplaner.model.Organisation;
import de.teamplaner.model.enums.Rolle;
import de.teamplaner.repository.BenutzerRepository;
import de.teamplaner.repository.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BenutzerRepository benutzerRepository;
    private final OrganisationRepository organisationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        if (benutzerRepository.count() == 0) {
            Organisation org = organisationRepository.findAll().stream().findFirst()
                    .orElseGet(() -> organisationRepository.save(
                            Organisation.builder().name("Standard-Organisation").build()));

            String initialPasswort = System.getenv().getOrDefault("ADMIN_INITIAL_PASSWORD", "admin");
            Benutzer admin = Benutzer.builder()
                    .benutzername("admin")
                    .passwort(passwordEncoder.encode(initialPasswort))
                    .anzeigename("Administrator")
                    .rolle(Rolle.ADMIN)
                    .aktiv(true)
                    .organisation(org)
                    .build();
            benutzerRepository.save(admin);
            log.warn("Standard-Admin '{}' angelegt — Passwort bitte sofort ändern!", admin.getBenutzername());
        }
    }
}
