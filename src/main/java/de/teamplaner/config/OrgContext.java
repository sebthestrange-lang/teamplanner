package de.teamplaner.config;

import de.teamplaner.model.Organisation;
import de.teamplaner.repository.BenutzerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrgContext {

    private final BenutzerRepository benutzerRepository;

    /** Org-ID des aktuell eingeloggten Benutzers, oder -1 wenn nicht authentifiziert. */
    public Long getOrgId() {
        String name = getPrincipalName();
        if (name == null) return -1L;
        return benutzerRepository.findByBenutzername(name)
                .map(b -> b.getOrganisation().getId())
                .orElse(-1L);
    }

    /** Organisation des aktuell eingeloggten Benutzers. */
    public Organisation getOrganisation() {
        String name = getPrincipalName();
        if (name == null) throw new IllegalStateException("Kein Benutzer authentifiziert");
        return benutzerRepository.findByBenutzername(name)
                .map(de.teamplaner.model.Benutzer::getOrganisation)
                .orElseThrow(() -> new IllegalStateException("Benutzer nicht gefunden: " + name));
    }

    private String getPrincipalName() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(String.valueOf(auth.getPrincipal()))) {
            return null;
        }
        return auth.getName();
    }
}
