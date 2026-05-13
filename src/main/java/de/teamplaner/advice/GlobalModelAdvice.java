package de.teamplaner.advice;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final BuildProperties buildProperties;

    @ModelAttribute("appVersion")
    public String appVersion() {
        return buildProperties.getVersion().replace("-SNAPSHOT", "");
    }
}
