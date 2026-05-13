package de.teamplaner.controller;

import de.teamplaner.dto.ProjektFilterDTO;
import de.teamplaner.model.Aufgabe;
import de.teamplaner.model.Projekt;
import de.teamplaner.model.enums.AufgabenStatus;
import de.teamplaner.model.enums.ProjektStatus;
import de.teamplaner.service.AufgabeService;
import de.teamplaner.service.ProjektService;
import de.teamplaner.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projekte")
@RequiredArgsConstructor
public class ProjektController {

    private final ProjektService projektService;
    private final TeamService teamService;
    private final AufgabeService aufgabeService;

    @GetMapping
    public String liste(@ModelAttribute ProjektFilterDTO filter, Model model) {
        model.addAttribute("projekte", projektService.mitFilter(filter));
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("projektStatus", ProjektStatus.values());
        model.addAttribute("filter", filter);
        return "projekte/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Projekt projekt = projektService.findByIdOrThrow(id);
        List<Aufgabe> aufgaben = aufgabeService.findByProjektId(id);

        long gesamt = aufgaben.size();
        long erledigt = aufgaben.stream()
                .filter(a -> a.getStatus() == AufgabenStatus.ABGESCHLOSSEN)
                .count();
        int fortschritt = gesamt > 0 ? (int) (erledigt * 100 / gesamt) : 0;

        model.addAttribute("projekt", projekt);
        model.addAttribute("aufgaben", aufgaben);
        model.addAttribute("beteiligte", projektService.getBeteiligteMitarbeiter(id));
        model.addAttribute("aufgabenStatistik", projektService.getAufgabenStatistikJeMitarbeiter(id));
        model.addAttribute("fortschritt", fortschritt);
        model.addAttribute("erledigteAufgaben", erledigt);
        model.addAttribute("gesamtAufgaben", gesamt);
        return "projekte/detail";
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        Projekt projekt = new Projekt();
        projekt.setStatus(ProjektStatus.GEPLANT);
        model.addAttribute("projekt", projekt);
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("projektStatus", ProjektStatus.values());
        model.addAttribute("aktion", "Neues Projekt");
        return "projekte/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("projekt") Projekt projekt,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("projektStatus", ProjektStatus.values());
            model.addAttribute("aktion", "Neues Projekt");
            return "projekte/formular";
        }
        projektService.speichern(projekt);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Projekt \"" + projekt.getName() + "\" wurde erstellt.");
        return "redirect:/projekte";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("projekt", projektService.findByIdOrThrow(id));
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("projektStatus", ProjektStatus.values());
        model.addAttribute("aktion", "Projekt bearbeiten");
        return "projekte/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("projekt") Projekt formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("teams", teamService.alleTeams());
            model.addAttribute("projektStatus", ProjektStatus.values());
            model.addAttribute("aktion", "Projekt bearbeiten");
            return "projekte/formular";
        }
        Projekt projekt = projektService.findByIdOrThrow(id);
        projekt.setName(formDaten.getName());
        projekt.setBeschreibung(formDaten.getBeschreibung());
        projekt.setStatus(formDaten.getStatus());
        projekt.setFarbe(formDaten.getFarbe());
        projekt.setFaelligAm(formDaten.getFaelligAm());
        projekt.setTeam(formDaten.getTeam());
        projektService.speichern(projekt);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Projekt wurde aktualisiert.");
        return "redirect:/projekte/" + id;
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        projektService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Projekt wurde gelöscht.");
        return "redirect:/projekte";
    }
}
