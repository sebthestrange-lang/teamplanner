package de.teamplaner.dto;

import de.teamplaner.model.enums.Rolle;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BenutzerFormDTO {
    private Long id;

    @NotBlank
    private String benutzername;

    @NotBlank
    private String anzeigename;

    @NotNull
    private Rolle rolle;

    private String passwort;
    private String passwortBestaetigung;
    private boolean aktiv = true;
    private Long organisationId;
}
