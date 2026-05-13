package de.teamplaner.dto;

import lombok.Data;

@Data
public class MitarbeiterFilterDTO {
    private Long teamId;
    private String rolle;
    private String suche;
}
