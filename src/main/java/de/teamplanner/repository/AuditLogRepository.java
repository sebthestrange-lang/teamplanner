package de.teamplanner.repository;

import de.teamplanner.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop500ByOrderByZeitpunktDesc();
}
