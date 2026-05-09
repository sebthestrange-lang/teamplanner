package de.teamplanner.controller;

import de.teamplanner.model.Meeting;
import de.teamplanner.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("meetings", meetingService.alle());
        return "meetings/liste";
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
        return "meetings/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("meeting") Meeting meeting,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Neues Meeting");
            return "meetings/formular";
        }
        meetingService.speichern(meeting);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Meeting \"" + meeting.getTitel() + "\" wurde erstellt.");
        return "redirect:/meetings";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("meeting", meetingService.findByIdOrThrow(id));
        model.addAttribute("aktion", "Meeting bearbeiten");
        return "meetings/formular";
    }

    @PostMapping("/{id}")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("meeting") Meeting formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Meeting bearbeiten");
            return "meetings/formular";
        }
        Meeting meeting = meetingService.findByIdOrThrow(id);
        meeting.setTitel(formDaten.getTitel());
        meeting.setDatum(formDaten.getDatum());
        meeting.setNotizen(formDaten.getNotizen());
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
}
