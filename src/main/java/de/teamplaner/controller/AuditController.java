package de.teamplaner.controller;

import de.teamplaner.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("eintraege", auditLogRepository.findTop500ByOrderByZeitpunktDesc());
        return "audit/liste";
    }
}
