package de.teamplaner.model;

import de.teamplaner.model.enums.Rolle;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "benutzer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Benutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String benutzername;

    @Column(nullable = false)
    private String passwort;

    @Column(nullable = false, length = 200)
    private String anzeigename;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rolle rolle;

    @Builder.Default
    private boolean aktiv = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Column(name = "dashboard_layout", columnDefinition = "TEXT")
    private String dashboardLayout;
}
