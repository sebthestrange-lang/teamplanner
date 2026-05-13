package de.teamplaner.controller;

import de.teamplaner.dto.TeamAuslastungDTO;
import de.teamplaner.model.Team;
import de.teamplaner.service.AufgabeService;
import de.teamplaner.service.MitarbeiterService;
import de.teamplaner.service.NotizService;
import de.teamplaner.service.ProjektService;
import de.teamplaner.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final MitarbeiterService mitarbeiterService;
    private final ProjektService projektService;
    private final AufgabeService aufgabeService;
    private final NotizService notizService;

    @GetMapping
    public String liste(Model model) {
        List<Team> teams = teamService.alleTeams();
        Map<Long, TeamAuslastungDTO> auslastungen = new LinkedHashMap<>();
        Map<Long, List<de.teamplaner.model.Mitarbeiter>> mitarbeiterByTeam = new LinkedHashMap<>();
        for (Team t : teams) {
            auslastungen.put(t.getId(), aufgabeService.auslastungFuerTeam(t));
            mitarbeiterByTeam.put(t.getId(), mitarbeiterService.findByTeam(t));
        }
        model.addAttribute("teams", teams);
        model.addAttribute("auslastungen", auslastungen);
        model.addAttribute("mitarbeiterByTeam", mitarbeiterByTeam);
        return "teams/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Team team = teamService.findByIdOrThrow(id);
        model.addAttribute("team", team);
        model.addAttribute("mitarbeiter", mitarbeiterService.findByTeam(team));
        model.addAttribute("verfuegbareMitarbeiter", mitarbeiterService.findOhneTeam());
        model.addAttribute("projekte", projektService.findByTeam(team));
        model.addAttribute("auslastung", aufgabeService.auslastungFuerTeam(team));
        model.addAttribute("notizen", notizService.findByTeam(team));
        return "teams/detail";
    }

    @PostMapping("/{id}/notizen")
    public String notizHinzufuegen(@PathVariable Long id,
                                   @RequestParam String inhalt,
                                   RedirectAttributes redirectAttributes) {
        Team team = teamService.findByIdOrThrow(id);
        if (inhalt != null && !inhalt.isBlank()) {
            notizService.hinzufuegenFuerTeam(inhalt.trim(), team);
        }
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/notizen/{notizId}/loeschen")
    public String notizLoeschen(@PathVariable Long id,
                                @PathVariable Long notizId,
                                RedirectAttributes redirectAttributes) {
        notizService.loeschen(notizId);
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/mitarbeiter/zuweisen")
    public String mitarbeiterZuweisen(@PathVariable Long id,
                                      @RequestParam Long mitarbeiterId,
                                      RedirectAttributes redirectAttributes) {
        Team team = teamService.findByIdOrThrow(id);
        mitarbeiterService.zuTeamZuweisen(mitarbeiterId, team);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Mitarbeiter wurde dem Team zugewiesen.");
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/mitarbeiter/{mitarbeiterId}/entfernen")
    public String mitarbeiterEntfernen(@PathVariable Long id,
                                       @PathVariable Long mitarbeiterId,
                                       RedirectAttributes redirectAttributes) {
        mitarbeiterService.ausTeamEntfernen(mitarbeiterId);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Mitarbeiter wurde aus dem Team entfernt.");
        return "redirect:/teams/" + id;
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        model.addAttribute("team", new Team());
        model.addAttribute("aktion", "Neues Team");
        return "teams/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("team") Team team,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Neues Team");
            return "teams/formular";
        }
        teamService.speichern(team);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Team \"" + team.getName() + "\" wurde erstellt.");
        return "redirect:/teams";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("team", teamService.findByIdOrThrow(id));
        model.addAttribute("aktion", "Team bearbeiten");
        return "teams/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("team") Team formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Team bearbeiten");
            return "teams/formular";
        }
        Team team = teamService.findByIdOrThrow(id);
        team.setName(formDaten.getName());
        team.setFarbe(formDaten.getFarbe());
        teamService.speichern(team);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Team wurde aktualisiert.");
        return "redirect:/teams/" + id;
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        teamService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Team wurde gelöscht.");
        return "redirect:/teams";
    }
}
