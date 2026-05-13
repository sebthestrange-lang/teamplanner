package de.teamplaner.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AufgabenStatus {
    OFFEN("Offen",                 "secondary"),
    IN_BEARBEITUNG("In Bearbeitung","primary"),
    ABGESCHLOSSEN("Abgeschlossen", "success");

    private final String bezeichnung;
    private final String bootstrapKlasse;
}
