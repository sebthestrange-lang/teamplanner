package de.teamplanner.advice;

import de.teamplanner.service.AufgabeService;
import de.teamplanner.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@ControllerAdvice
@RequiredArgsConstructor
public class NavbarModelAdvice {

    private final TodoService todoService;
    private final AufgabeService aufgabeService;

    @ModelAttribute("sidebarOffeneTodos")
    public long sidebarOffeneTodos() {
        try {
            return todoService.anzahlOffen();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("sidebarUeberfaelligeAufgaben")
    public long sidebarUeberfaelligeAufgaben() {
        try {
            return aufgabeService.anzahlUeberfaellig();
        } catch (Exception e) {
            return 0;
        }
    }

    @ModelAttribute("currentUri")
    public String currentUri() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes)
                    RequestContextHolder.currentRequestAttributes();
            return attrs.getRequest().getRequestURI();
        } catch (IllegalStateException e) {
            return "";
        }
    }
}
