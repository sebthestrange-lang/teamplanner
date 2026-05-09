package de.teamplanner.controller;

import de.teamplanner.dto.BenutzerFormDTO;
import de.teamplanner.model.Benutzer;
import de.teamplanner.model.enums.Rolle;
import de.teamplanner.service.BenutzerService;
import de.teamplanner.service.OrganisationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/benutzer")
@RequiredArgsConstructor
public class BenutzerController {

    private final BenutzerService benutzerService;
    private final OrganisationService organisationService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("benutzer", benutzerService.findAll());
        return "benutzer/liste";
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        BenutzerFormDTO dto = new BenutzerFormDTO();
        dto.setAktiv(true);
        model.addAttribute("benutzerForm", dto);
        model.addAttribute("rollen", Rolle.values());
        model.addAttribute("organisationen", organisationService.alle());
        model.addAttribute("aktion", "Neuer Benutzer");
        model.addAttribute("istNeu", true);
        return "benutzer/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("benutzerForm") BenutzerFormDTO dto,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (!result.hasErrors()) {
            if (dto.getPasswort() == null || dto.getPasswort().isBlank()) {
                result.rejectValue("passwort", "passwort.required", "Passwort ist erforderlich.");
            } else if (dto.getPasswort().length() < 8) {
                result.rejectValue("passwort", "passwort.tooShort", "Passwort muss mindestens 8 Zeichen haben.");
            } else if (!dto.getPasswort().equals(dto.getPasswortBestaetigung())) {
                result.rejectValue("passwortBestaetigung", "passwort.mismatch", "Passwörter stimmen nicht überein.");
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("rollen", Rolle.values());
            model.addAttribute("organisationen", organisationService.alle());
            model.addAttribute("aktion", "Neuer Benutzer");
            model.addAttribute("istNeu", true);
            return "benutzer/formular";
        }
        benutzerService.speichern(dto, null);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Benutzer '" + dto.getBenutzername() + "' wurde angelegt.");
        return "redirect:/benutzer";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        Benutzer benutzer = benutzerService.findByIdOrThrow(id);
        BenutzerFormDTO dto = new BenutzerFormDTO();
        dto.setId(benutzer.getId());
        dto.setBenutzername(benutzer.getBenutzername());
        dto.setAnzeigename(benutzer.getAnzeigename());
        dto.setRolle(benutzer.getRolle());
        dto.setAktiv(benutzer.isAktiv());
        model.addAttribute("benutzerForm", dto);
        model.addAttribute("rollen", Rolle.values());
        model.addAttribute("aktion", "Benutzer bearbeiten");
        model.addAttribute("istNeu", false);
        return "benutzer/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("benutzerForm") BenutzerFormDTO dto,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (!result.hasErrors() && org.springframework.util.StringUtils.hasText(dto.getPasswort())) {
            if (dto.getPasswort().length() < 8) {
                result.rejectValue("passwort", "passwort.tooShort", "Passwort muss mindestens 8 Zeichen haben.");
            } else if (!dto.getPasswort().equals(dto.getPasswortBestaetigung())) {
                result.rejectValue("passwortBestaetigung", "passwort.mismatch", "Passwörter stimmen nicht überein.");
            }
        }
        if (result.hasErrors()) {
            model.addAttribute("rollen", Rolle.values());
            model.addAttribute("aktion", "Benutzer bearbeiten");
            model.addAttribute("istNeu", false);
            return "benutzer/formular";
        }
        Benutzer existing = benutzerService.findByIdOrThrow(id);
        try {
            benutzerService.speichern(dto, existing);
        } catch (IllegalStateException e) {
            result.reject("benutzer.selbst", e.getMessage());
            model.addAttribute("rollen", Rolle.values());
            model.addAttribute("aktion", "Benutzer bearbeiten");
            model.addAttribute("istNeu", false);
            return "benutzer/formular";
        }
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Benutzer wurde aktualisiert.");
        return "redirect:/benutzer";
    }

    @GetMapping("/{id}/passwort")
    public String passwortFormular(@PathVariable Long id, Model model) {
        model.addAttribute("benutzer", benutzerService.findByIdOrThrow(id));
        return "benutzer/passwort";
    }

    @PostMapping("/{id}/passwort")
    public String passwortSetzen(@PathVariable Long id,
                                 @RequestParam String neuesPasswort,
                                 @RequestParam String passwortBestaetigung,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        de.teamplanner.model.Benutzer benutzer = benutzerService.findByIdOrThrow(id);
        if (neuesPasswort == null || neuesPasswort.isBlank()) {
            model.addAttribute("benutzer", benutzer);
            model.addAttribute("fehler", "Passwort darf nicht leer sein.");
            return "benutzer/passwort";
        }
        if (neuesPasswort.length() < 8) {
            model.addAttribute("benutzer", benutzer);
            model.addAttribute("fehler", "Passwort muss mindestens 8 Zeichen haben.");
            return "benutzer/passwort";
        }
        if (!neuesPasswort.equals(passwortBestaetigung)) {
            model.addAttribute("benutzer", benutzer);
            model.addAttribute("fehler", "Passwörter stimmen nicht überein.");
            return "benutzer/passwort";
        }
        benutzerService.passwortSetzen(id, neuesPasswort);
        redirectAttributes.addFlashAttribute("erfolgsMeldung",
                "Passwort für '" + benutzer.getBenutzername() + "' wurde geändert.");
        return "redirect:/benutzer";
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            benutzerService.loeschen(id);
            redirectAttributes.addFlashAttribute("erfolgsMeldung", "Benutzer wurde gelöscht.");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("fehlerMeldung", e.getMessage());
        }
        return "redirect:/benutzer";
    }

}
