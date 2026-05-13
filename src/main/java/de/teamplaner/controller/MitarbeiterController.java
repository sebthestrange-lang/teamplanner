package de.teamplaner.controller;

import de.teamplaner.dto.MitarbeiterFilterDTO;
import de.teamplaner.model.Mitarbeiter;
import de.teamplaner.service.AufgabeService;
import de.teamplaner.service.MitarbeiterService;
import de.teamplaner.service.NotizService;
import de.teamplaner.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/mitarbeiter")
@RequiredArgsConstructor
public class MitarbeiterController {

    private final MitarbeiterService mitarbeiterService;
    private final TeamService teamService;
    private final AufgabeService aufgabeService;
    private final NotizService notizService;

    @GetMapping
    public String liste(@ModelAttribute MitarbeiterFilterDTO filter, Model model) {
        model.addAttribute("mitarbeiter", mitarbeiterService.mitFilter(filter));
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("filter", filter);
        return "mitarbeiter/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Mitarbeiter mitarbeiter = mitarbeiterService.findByIdOrThrow(id);
        model.addAttribute("mitarbeiter", mitarbeiter);
        model.addAttribute("aufgaben", aufgabeService.findByMitarbeiter(mitarbeiter));
        model.addAttribute("notizen", notizService.findByMitarbeiter(mitarbeiter));
        return "mitarbeiter/detail";
    }

    @PostMapping("/{id}/notizen")
    public String notizHinzufuegen(@PathVariable Long id,
                                   @RequestParam String inhalt,
                                   RedirectAttributes redirectAttributes) {
        Mitarbeiter mitarbeiter = mitarbeiterService.findByIdOrThrow(id);
        if (inhalt != null && !inhalt.isBlank()) {
            notizService.hinzufuegenFuerMitarbeiter(inhalt.trim(), mitarbeiter);
        }
        return "redirect:/mitarbeiter/" + id;
    }

    @PostMapping("/{id}/notizen/{notizId}/loeschen")
    public String notizLoeschen(@PathVariable Long id,
                                @PathVariable Long notizId,
                                RedirectAttributes redirectAttributes) {
        notizService.loeschen(notizId);
        return "redirect:/mitarbeiter/" + id;
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        model.addAttribute("mitarbeiter", new Mitarbeiter());
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("aktion", "Neuer Mitarbeiter");
        return "mitarbeiter/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("mitarbeiter") Mitarbeiter mitarbeiter,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("aktion", "Neuer Mitarbeiter");
            return "mitarbeiter/formular";
        }
        mitarbeiterService.speichern(mitarbeiter);
        redirectAttributes.addFlashAttribute("erfolgsMeldung",
                mitarbeiter.getVollstaendigerName() + " wurde hinzugefügt.");
        return "redirect:/mitarbeiter";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("mitarbeiter", mitarbeiterService.findByIdOrThrow(id));
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("aktion", "Mitarbeiter bearbeiten");
        return "mitarbeiter/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("mitarbeiter") Mitarbeiter formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("aktion", "Mitarbeiter bearbeiten");
            return "mitarbeiter/formular";
        }
        Mitarbeiter mitarbeiter = mitarbeiterService.findByIdOrThrow(id);
        mitarbeiter.setVorname(formDaten.getVorname());
        mitarbeiter.setNachname(formDaten.getNachname());
        mitarbeiter.setRolle(formDaten.getRolle());
        mitarbeiter.setEmail(formDaten.getEmail());
        mitarbeiter.setTelefon(formDaten.getTelefon());
        mitarbeiter.setTeam(formDaten.getTeam());
        mitarbeiterService.speichern(mitarbeiter);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Mitarbeiter wurde aktualisiert.");
        return "redirect:/mitarbeiter/" + id;
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        mitarbeiterService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Mitarbeiter wurde gelöscht.");
        return "redirect:/mitarbeiter";
    }
}
