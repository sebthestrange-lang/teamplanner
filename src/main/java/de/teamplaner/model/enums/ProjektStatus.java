package de.teamplaner.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProjektStatus {
    GEPLANT("Geplant",           "secondary"),
    AKTIV("Aktiv",               "primary"),
    ABGESCHLOSSEN("Abgeschlossen","success");

    private final String bezeichnung;
    private final String bootstrapKlasse;
}
