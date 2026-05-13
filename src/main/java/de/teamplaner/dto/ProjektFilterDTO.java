package de.teamplaner.dto;

import de.teamplaner.model.enums.ProjektStatus;
import lombok.Data;

@Data
public class ProjektFilterDTO {
    private Long teamId;
    private ProjektStatus status;
    private String faelligkeit;
    private String suche;
}
