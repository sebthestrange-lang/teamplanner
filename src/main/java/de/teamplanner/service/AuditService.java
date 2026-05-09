package de.teamplanner.service;

import de.teamplanner.model.AuditLog;
import de.teamplanner.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public void log(String entityType, Long entityId, String aktion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String benutzername;
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            benutzername = "system";
        } else {
            benutzername = auth.getName();
        }
        auditLogRepository.save(AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .aktion(aktion)
                .benutzername(benutzername)
                .zeitpunkt(LocalDateTime.now())
                .build());
    }
}
