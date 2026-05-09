package de.teamplanner.controller;

import de.teamplanner.dto.TeamAuslastungDTO;
import de.teamplanner.model.Aufgabe;
import de.teamplanner.model.Mitarbeiter;
import de.teamplanner.model.Team;
import de.teamplanner.model.enums.AufgabenStatus;
import de.teamplanner.service.AufgabeService;
import de.teamplanner.service.BenutzerService;
import de.teamplanner.service.MitarbeiterService;
import de.teamplanner.service.ProjektService;
import de.teamplanner.service.TeamService;
import de.teamplanner.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class DashboardController {

    private final TeamService teamService;
    private final MitarbeiterService mitarbeiterService;
    private final ProjektService projektService;
    private final AufgabeService aufgabeService;
    private final TodoService todoService;
    private final BenutzerService benutzerService;

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("anzahlTeams", teamService.anzahl());
        model.addAttribute("anzahlMitarbeiter", mitarbeiterService.anzahl());
        model.addAttribute("offeneAufgaben", aufgabeService.anzahlOffen());
        model.addAttribute("ueberfaelligeAufgaben", aufgabeService.anzahlUeberfaellig());
        model.addAttribute("heuteFaelligeAufgaben", aufgabeService.heuteFaellig());
        model.addAttribute("letzteAktivitaeten", aufgabeService.letzteAktivitaeten(8));
        model.addAttribute("auslastung", berechneAuslastung());
        model.addAttribute("offeneTodos", todoService.anzahlOffen());
        model.addAttribute("heuteFaelligeTodos", todoService.anzahlHeuteFaellig());
        model.addAttribute("ueberfaelligeTodos", todoService.anzahlUeberfaellig());

        model.addAttribute("chartStatusOffen",         aufgabeService.anzahlNachStatus(AufgabenStatus.OFFEN));
        model.addAttribute("chartStatusInBearbeitung", aufgabeService.anzahlNachStatus(AufgabenStatus.IN_BEARBEITUNG));
        model.addAttribute("chartStatusAbgeschlossen", aufgabeService.anzahlNachStatus(AufgabenStatus.ABGESCHLOSSEN));

        List<Team> teams = teamService.alleTeams();
        Map<Long, TeamAuslastungDTO> teamAuslastungen = new LinkedHashMap<>();
        for (Team t : teams) {
            teamAuslastungen.put(t.getId(), aufgabeService.auslastungFuerTeam(t));
        }
        model.addAttribute("dashboardTeams", teams);
        model.addAttribute("dashboardTeamAuslastungen", teamAuslastungen);
        model.addAttribute("chartTeamNamen",  teams.stream().map(Team::getName).collect(Collectors.toList()));
        model.addAttribute("chartTeamFarben", teams.stream().map(Team::getFarbe).collect(Collectors.toList()));
        model.addAttribute("chartTeamAktiv",  teams.stream()
                .map(t -> teamAuslastungen.get(t.getId()).aktiv())
                .collect(Collectors.toList()));

        model.addAttribute("layoutJson", benutzerService.getDashboardLayout());

        return "dashboard";
    }

    @PostMapping("dashboard/layout")
    public String layoutSpeichern(@RequestParam String layoutJson) {
        benutzerService.dashboardLayoutSpeichern(layoutJson);
        return "redirect:/";
    }

    @PostMapping("dashboard/layout/reset")
    public String layoutZuruecksetzen() {
        benutzerService.dashboardLayoutSpeichern(null);
        return "redirect:/";
    }

    private Map<Mitarbeiter, Long> berechneAuslastung() {
        return aufgabeService.alleAufgaben()
                .stream()
                .filter(a -> a.getMitarbeiter() != null)
                .filter(a -> a.getStatus() != AufgabenStatus.ABGESCHLOSSEN)
                .collect(Collectors.groupingBy(Aufgabe::getMitarbeiter, Collectors.counting()));
    }
}
