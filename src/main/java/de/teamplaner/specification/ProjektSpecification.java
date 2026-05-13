package de.teamplaner.specification;

import de.teamplaner.dto.ProjektFilterDTO;
import de.teamplaner.model.Projekt;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class ProjektSpecification {

    private ProjektSpecification() {}

    public static Specification<Projekt> withFilter(ProjektFilterDTO filter) {
        return hatTeam(filter.getTeamId())
                .and(hatStatus(filter.getStatus()))
                .and(hatFaelligkeit(filter.getFaelligkeit()))
                .and(enthältSuchtext(filter.getSuche()));
    }

    private static Specification<Projekt> hatTeam(Long teamId) {
        if (teamId == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("team").get("id"), teamId);
    }

    private static Specification<Projekt> hatStatus(de.teamplaner.model.enums.ProjektStatus status) {
        if (status == null) return Specification.unrestricted();
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    private static Specification<Projekt> hatFaelligkeit(String faelligkeit) {
        if (!StringUtils.hasText(faelligkeit)) return Specification.unrestricted();
        return switch (faelligkeit) {
            case "ueberfaellig" -> (root, query, cb) ->
                    cb.lessThan(root.get("faelligAm"), LocalDate.now());
            case "woche" -> (root, query, cb) ->
                    cb.between(root.get("faelligAm"), LocalDate.now(), LocalDate.now().plusDays(7));
            default -> Specification.unrestricted();
        };
    }

    private static Specification<Projekt> enthältSuchtext(String suche) {
        if (!StringUtils.hasText(suche)) return Specification.unrestricted();
        String muster = "%" + suche.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), muster),
                cb.like(cb.lower(root.get("beschreibung")), muster)
        );
    }
}
