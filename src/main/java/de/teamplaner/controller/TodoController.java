package de.teamplaner.controller;

import de.teamplaner.dto.TodoFilterDTO;
import de.teamplaner.model.Todo;
import de.teamplaner.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    public String liste(@ModelAttribute TodoFilterDTO filter, Model model) {
        model.addAttribute("todos", todoService.mitFilter(filter));
        model.addAttribute("filter", filter);
        model.addAttribute("schnellTodo", new Todo());
        return "todos/liste";
    }

    @GetMapping("/neu")
    public String neuFormular(Model model) {
        model.addAttribute("todo", new Todo());
        model.addAttribute("aktion", "Neues Todo");
        return "todos/formular";
    }

    @PostMapping
    public String erstellen(@Valid @ModelAttribute("schnellTodo") Todo todo,
                            BindingResult result,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("todos", todoService.mitFilter(new TodoFilterDTO()));
            model.addAttribute("filter", new TodoFilterDTO());
            return "todos/liste";
        }
        todoService.speichern(todo);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Todo wurde angelegt.");
        return "redirect:/todos";
    }

    @GetMapping("/{id}/bearbeiten")
    public String bearbeitenFormular(@PathVariable Long id, Model model) {
        model.addAttribute("todo", todoService.findByIdOrThrow(id));
        model.addAttribute("aktion", "Todo bearbeiten");
        return "todos/formular";
    }

    @PostMapping("/{id}/bearbeiten")
    public String aktualisieren(@PathVariable Long id,
                                @Valid @ModelAttribute("todo") Todo formDaten,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("aktion", "Todo bearbeiten");
            return "todos/formular";
        }
        Todo todo = todoService.findByIdOrThrow(id);
        todo.setTitel(formDaten.getTitel());
        todo.setBeschreibung(formDaten.getBeschreibung());
        todo.setPrioritaet(formDaten.getPrioritaet());
        todo.setFaelligAm(formDaten.getFaelligAm());
        todoService.speichern(todo);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Todo wurde aktualisiert.");
        return "redirect:/todos";
    }

    @PostMapping("/{id}/erledigt")
    public String erledigtToggle(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Todo todo = todoService.erledigtToggle(id);
        String meldung = todo.isErledigt() ? "Todo als erledigt markiert." : "Todo wieder geöffnet.";
        redirectAttributes.addFlashAttribute("erfolgsMeldung", meldung);
        return "redirect:/todos";
    }

    @PostMapping("/{id}/loeschen")
    public String loeschen(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        todoService.loeschen(id);
        redirectAttributes.addFlashAttribute("erfolgsMeldung", "Todo wurde gelöscht.");
        return "redirect:/todos";
    }
}
