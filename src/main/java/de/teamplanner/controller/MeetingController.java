package de.teamplanner.controller;

import de.teamplanner.model.Meeting;
import de.teamplanner.service.MeetingService;
import de.teamplanner.service.TeamService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final TeamService teamService;

    @GetMapping
    public String liste(Model model) {
        List<Meeting> meetings = meetingService.alle();
        model.addAttribute("meetings", meetings);

        List<Map<String, Object>> calendarEvents = meetings.stream()
                .map(m -> {
                    Map<String, Object> ev = new LinkedHashMap<>();
                    ev.put("title", m.getTitel());
                    ev.put("start", m.getDatum().toString());
                    ev.put("url", "/meetings/" + m.getId());
                    if (m.getTeam() != null) {
                        ev.put("color", m.getTeam().getFarbe());
                    }
                    return ev;
                })
                .collect(Collectors.toList());
        model.addAttribute("calendarEvents", calendarEvents);

        return "meetings/liste";
    }

    @GetMapping("/export.csv")
    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"meetings.csv\"");
        PrintWriter writer = response.getWriter();
        writer.print('﻿');
        writer.println("Datum,Titel,Team,Notizen");
        for (Meeting m : meetingService.alle()) {
            writer.println(
                    csvEscape(m.getDatum().toString()) + "," +
                    csvEscape(m.getTitel()) + "," +
                    csvEscape(m.getTeam() != null ? m.getTeam().getName() : "") + "," +
                    csvEscape(m.getNotizen() != null ? m.getNotizen() : "")
            );
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("meeting", meetingService.findByIdOrThrow(id));
        return "meetings/detail";
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        model.addAttribute("meeting", new Meeting());
        model.addAttribute("aktion", "Neues Meeting");
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("ausgewaehlteTeamId", null);
        return "meetings/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("meeting") Meeting meeting,
                            BindingResult result,
                            @RequestParam(required = false) Long teamId,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Neues Meeting");
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("ausgewaehlteTeamId", teamId);
            return "meetings/formular";
        }
        meeting.setTeam(teamId != null ? teamService.findByIdOrThrow(teamId) : null);
        meetingService.speichern(meeting);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Meeting \"" + meeting.getTitel() + "\" wurde erstellt.");
        return "redirect:/meetings";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        Meeting meeting = meetingService.findByIdOrThrow(id);
        model.addAttribute("meeting", meeting);
        model.addAttribute("aktion", "Meeting bearbeiten");
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("ausgewaehlteTeamId", meeting.getTeam() != null ? meeting.getTeam().getId() : null);
        return "meetings/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("meeting") Meeting formDaten,
                                BindingResult result,
                                @RequestParam(required = false) Long teamId,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Meeting bearbeiten");
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("ausgewaehlteTeamId", teamId);
            return "meetings/formular";
        }
        Meeting meeting = meetingService.findByIdOrThrow(id);
        meeting.setTitel(formDaten.getTitel());
        meeting.setDatum(formDaten.getDatum());
        meeting.setNotizen(formDaten.getNotizen());
        meeting.setTeam(teamId != null ? teamService.findByIdOrThrow(teamId) : null);
        meetingService.speichern(meeting);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Meeting wurde aktualisiert.");
        return "redirect:/meetings/" + id;
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        meetingService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Meeting wurde gelöscht.");
        return "redirect:/meetings";
    }

    private String csvEscape(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
