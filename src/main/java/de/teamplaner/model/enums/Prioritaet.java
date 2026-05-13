package de.teamplaner.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Prioritaet {
    NIEDRIG("Niedrig", "#10b981", "success"),
    MITTEL("Mittel",   "#f59e0b", "warning"),
    HOCH("Hoch",       "#f97316", "orange"),
    KRITISCH("Kritisch","#ef4444","danger");

    private final String bezeichnung;
    private final String farbe;
    private final String bootstrapKlasse;
}
