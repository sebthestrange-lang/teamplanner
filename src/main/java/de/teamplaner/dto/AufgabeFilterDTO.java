package de.teamplaner.dto;

import de.teamplaner.model.enums.AufgabenStatus;
import de.teamplaner.model.enums.Prioritaet;
import lombok.Data;

@Data
public class AufgabeFilterDTO {
    private Long teamId;
    private Long mitarbeiterId;
    private Long projektId;
    private AufgabenStatus status;
    private Prioritaet prioritaet;
    private String faelligkeit;
    private String suche;
}
