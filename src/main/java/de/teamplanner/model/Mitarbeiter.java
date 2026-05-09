package de.teamplanner.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "mitarbeiter")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
public class Mitarbeiter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Column(name = "vorname", nullable = false)
    private String vorname;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Column(name = "nachname", nullable = false)
    private String nachname;

    @Column(name = "rolle")
    private String rolle;

    @Email(message = "Bitte eine gültige E-Mail-Adresse angeben")
    @Column(name = "email")
    private String email;

    @Column(name = "telefon")
    private String telefon;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @CreationTimestamp
    @Column(name = "erstellt_am", nullable = false, updatable = false)
    private LocalDateTime erstelltAm;

    public String getVollstaendigerName() {
        return vorname + " " + nachname;
    }

    public String getInitialen() {
        return String.valueOf(vorname.charAt(0)).toUpperCase()
             + String.valueOf(nachname.charAt(0)).toUpperCase();
    }
}
