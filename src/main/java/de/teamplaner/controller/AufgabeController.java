package de.teamplaner.controller;

import de.teamplaner.dto.AufgabeFilterDTO;
import de.teamplaner.model.Aufgabe;
import de.teamplaner.model.enums.AufgabenStatus;
import de.teamplaner.model.enums.Prioritaet;
import de.teamplaner.service.AufgabeService;
import de.teamplaner.service.MitarbeiterService;
import de.teamplaner.service.ProjektService;
import de.teamplaner.service.TeamService;
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
import java.util.List;

@Controller
@RequestMapping("/aufgaben")
@RequiredArgsConstructor
public class AufgabeController {

    private final AufgabeService aufgabeService;
    private final ProjektService projektService;
    private final MitarbeiterService mitarbeiterService;
    private final TeamService teamService;

    @GetMapping
    public String liste(@ModelAttribute AufgabeFilterDTO filter, Model model) {
        model.addAttribute("aufgaben", aufgabeService.mitFilter(filter));
        model.addAttribute("teams", teamService.alleTeams());
        model.addAttribute("projekte", projektService.alleProjekte());
        model.addAttribute("mitarbeiter", mitarbeiterService.alleMitarbeiter());
        model.addAttribute("aufgabenStatus", AufgabenStatus.values());
        model.addAttribute("prioritaeten", Prioritaet.values());
        model.addAttribute("filter", filter);
        return "aufgaben/liste";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("aufgabe", aufgabeService.findByIdOrThrow(id));
        return "aufgaben/detail";
    }

    @GetMapping("/neu")
    public String neuFormular(@RequestParam(required = false) Long projektId, Model model) {
        Aufgabe aufgabe = new Aufgabe();
        aufgabe.setStatus(AufgabenStatus.OFFEN);
        aufgabe.setPrioritaet(Prioritaet.MITTEL);
        if (projektId != null) {
            aufgabe.setProjekt(projektService.findByIdOrThrow(projektId));
        }
        model.addAttribute("aufgabe", aufgabe);
        model.addAttribute("projekte", projektService.alleProjekte());
        model.addAttribute("mitarbeiter", mitarbeiterService.alleMitarbeiter());
        model.addAttribute("aufgabenStatus", AufgabenStatus.values());
        model.addAttribute("prioritaeten", Prioritaet.values());
        model.addAttribute("aktion", "Neue Aufgabe");
        return "aufgaben/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("aufgabe") Aufgabe aufgabe,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("projekte", projektService.alleProjekte());
            model.addAttribute("mitarbeiter", mitarbeiterService.alleMitarbeiter());
            model.addAttribute("aufgabenStatus", AufgabenStatus.values());
            model.addAttribute("prioritaeten", Prioritaet.values());
            model.addAttribute("aktion", "Neue Aufgabe");
            return "aufgaben/formular";
        }
        aufgabeService.speichern(aufgabe);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Aufgabe wurde erstellt.");
        return "redirect:/aufgaben";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("aufgabe", aufgabeService.findByIdOrThrow(id));
        model.addAttribute("projekte", projektService.alleProjekte());
        model.addAttribute("mitarbeiter", mitarbeiterService.alleMitarbeiter());
        model.addAttribute("aufgabenStatus", AufgabenStatus.values());
        model.addAttribute("prioritaeten", Prioritaet.values());
        model.addAttribute("aktion", "Aufgabe bearbeiten");
        return "aufgaben/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("aufgabe") Aufgabe formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("projekte", projektService.alleProjekte());
            model.addAttribute("mitarbeiter", mitarbeiterService.alleMitarbeiter());
            model.addAttribute("aufgabenStatus", AufgabenStatus.values());
            model.addAttribute("prioritaeten", Prioritaet.values());
            model.addAttribute("aktion", "Aufgabe bearbeiten");
            return "aufgaben/formular";
        }
        Aufgabe aufgabe = aufgabeService.findByIdOrThrow(id);
        aufgabe.setTitel(formDaten.getTitel());
        aufgabe.setBeschreibung(formDaten.getBeschreibung());
        aufgabe.setPrioritaet(formDaten.getPrioritaet());
        aufgabe.setProjekt(formDaten.getProjekt());
        aufgabe.setMitarbeiter(formDaten.getMitarbeiter());
        aufgabe.setFaelligAm(formDaten.getFaelligAm());
        aufgabeService.statusAendern(aufgabe, formDaten.getStatus());
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Aufgabe wurde aktualisiert.");
        return "redirect:/aufgaben/" + id;
    }

    @PostMapping("/{id}/status")
    public String statusAendern(@PathVariable Long id,
                                @RequestParam AufgabenStatus status,
                                RedirectAttributes redirectAttributes) {
        aufgabeService.statusAendern(id, status);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Status wurde aktualisiert.");
        return "redirect:/aufgaben/" + id;
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        aufgabeService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Aufgabe wurde gelöscht.");
        return "redirect:/aufgaben";
    }

    @GetMapping("/export")
    public void csvExport(@ModelAttribute AufgabeFilterDTO filter,
                          HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"aufgaben.csv\"");

        List<Aufgabe> aufgaben = aufgabeService.mitFilter(filter);
        PrintWriter writer = response.getWriter();
        writer.println("ID,Titel,Projekt,Mitarbeiter,Priorität,Status,Fällig am,Erstellt am");

        for (Aufgabe a : aufgaben) {
            writer.println(String.join(";",
                    String.valueOf(a.getId()),
                    csv(a.getTitel()),
                    csv(a.getProjekt() != null ? a.getProjekt().getName() : ""),
                    csv(a.getMitarbeiter() != null ? a.getMitarbeiter().getVollstaendigerName() : ""),
                    csv(a.getPrioritaet().getBezeichnung()),
                    csv(a.getStatus().getBezeichnung()),
                    a.getFaelligAm() != null ? a.getFaelligAm().toString() : "",
                    a.getErstelltAm() != null ? a.getErstelltAm().toLocalDate().toString() : ""
            ));
        }
        writer.flush();
    }

    private String csv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
