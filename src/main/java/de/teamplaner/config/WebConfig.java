package de.teamplaner.config;

import de.teamplaner.model.Mitarbeiter;
import de.teamplaner.model.Projekt;
import de.teamplaner.model.Team;
import de.teamplaner.repository.MitarbeiterRepository;
import de.teamplaner.repository.ProjektRepository;
import de.teamplaner.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TeamRepository teamRepository;
    private final ProjektRepository projektRepository;
    private final MitarbeiterRepository mitarbeiterRepository;

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new Formatter<Team>() {
            @Override
            public Team parse(String text, Locale locale) {
                if (!StringUtils.hasText(text)) return null;
                return teamRepository.findById(Long.valueOf(text)).orElse(null);
            }
            @Override
            public String print(Team t, Locale locale) {
                return (t == null || t.getId() == null) ? "" : String.valueOf(t.getId());
            }
        });

        registry.addFormatter(new Formatter<Projekt>() {
            @Override
            public Projekt parse(String text, Locale locale) {
                if (!StringUtils.hasText(text)) return null;
                return projektRepository.findById(Long.valueOf(text)).orElse(null);
            }
            @Override
            public String print(Projekt p, Locale locale) {
                return (p == null || p.getId() == null) ? "" : String.valueOf(p.getId());
            }
        });

        registry.addFormatter(new Formatter<Mitarbeiter>() {
            @Override
            public Mitarbeiter parse(String text, Locale locale) {
                if (!StringUtils.hasText(text)) return null;
                return mitarbeiterRepository.findById(Long.valueOf(text)).orElse(null);
            }
            @Override
            public String print(Mitarbeiter m, Locale locale) {
                return (m == null || m.getId() == null) ? "" : String.valueOf(m.getId());
            }
        });
    }
}
