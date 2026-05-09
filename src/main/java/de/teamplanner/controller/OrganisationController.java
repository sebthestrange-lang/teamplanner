package de.teamplanner.controller;

import de.teamplanner.model.Organisation;
import de.teamplanner.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/organisationen")
@RequiredArgsConstructor
public class OrganisationController {

    private final OrganisationService organisationService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("organisationen", organisationService.alle());
        return "organisationen/liste";
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        model.addAttribute("organisation", new Organisation());
        model.addAttribute("aktion", "Neue Organisation");
        return "organisationen/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("organisation") Organisation organisation,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Neue Organisation");
            return "organisationen/formular";
        }
        organisationService.speichern(organisation);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Organisation \"" + organisation.getName() + "\" wurde erstellt.");
        return "redirect:/organisationen";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("organisation", organisationService.findByIdOrThrow(id));
        model.addAttribute("aktion", "Organisation bearbeiten");
        return "organisationen/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("organisation") Organisation formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Organisation bearbeiten");
            return "organisationen/formular";
        }
        Organisation org = organisationService.findByIdOrThrow(id);
        org.setName(formDaten.getName());
        organisationService.speichern(org);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Organisation wurde aktualisiert.");
        return "redirect:/organisationen";
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        organisationService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Organisation wurde gelöscht.");
        return "redirect:/organisationen";
    }
}
