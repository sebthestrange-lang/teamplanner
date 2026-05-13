package de.teamplaner.specification;

import de.teamplaner.dto.AufgabeFilterDTO;
import de.teamplaner.model.Aufgabe;
import de.teamplaner.model.enums.AufgabenStatus;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class AufgabeSpecification {

    private AufgabeSpecification() {}

    public static Specification<Aufgabe> withFilter(AufgabeFilterDTO filter) {
        return hatTeam(filter.getTeamId())
                .and(hatMitarbeiter(filter.getMitarbeiterId()))
                .and(hatProjekt(filter.getProjektId()))
                .and(hatStatus(filter.getStatus()))
                .and(hatPrioritaet(filter.getPrioritaet()))
                .and(hatFaelligkeit(filter.getFaelligkeit()))
                .and(enthältSuchtext(filter.getSuche()));
    }

    private static Specification<Aufgabe> hatTeam(Long teamId) {
        if (teamId == null) return Specification.unrestricted();
        return (root, query, cb) ->
                cb.equal(root.get("projekt").get("team").get("id"), teamId);
    }

    private static Specification<Aufgabe> hatMitarbeiter(Long mitarbeiterId) {
        if (mitarbeiterId == null) return Specification.unrestricted();
        return (root, query, cb) ->
                cb.equal(root.get("mitarbeiter").get("id"), mitarbeiterId);
    }

    private static Specification<Aufgabe> hatProjekt(Long projektId) {
        if (projektId == null) return Specification.unrestricted();
        return (root, query, cb) ->
                cb.equal(root.get("projekt").get("id"), projektId);
    }

    private static Specification<Aufgabe> hatStatus(AufgabenStatus status) {
        if (status == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Aufgabe> hatPrioritaet(de.teamplaner.model.enums.Prioritaet prioritaet) {
        if (prioritaet == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("prioritaet"), prioritaet);
    }

    private static Specification<Aufgabe> hatFaelligkeit(String faelligkeit) {
        if (!StringUtils.hasText(faelligkeit)) return Specification.unrestricted();
        return switch (faelligkeit) {
            case "heute" -> (root, query, cb) ->
                    cb.equal(root.get("faelligAm"), LocalDate.now());
            case "woche" -> (root, query, cb) ->
                    cb.between(root.get("faelligAm"), LocalDate.now(), LocalDate.now().plusDays(7));
            case "ueberfaellig" -> (root, query, cb) -> cb.and(
                    cb.lessThan(root.get("faelligAm"), LocalDate.now()),
                    cb.notEqual(root.get("status"), AufgabenStatus.ABGESCHLOSSEN)
            );
            default -> Specification.unrestricted();
        };
    }

    private static Specification<Aufgabe> enthältSuchtext(String suche) {
        if (!StringUtils.hasText(suche)) return Specification.unrestricted();
        String muster = "%" + suche.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titel")), muster),
                cb.like(cb.lower(root.get("beschreibung")), muster)
        );
    }
}
