package de.teamplaner.specification;

import de.teamplaner.dto.TodoFilterDTO;
import de.teamplaner.model.Todo;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

public class TodoSpecification {

    private TodoSpecification() {}

    public static Specification<Todo> withFilter(TodoFilterDTO filter) {
        return hatStatus(filter.getStatus())
                .and(enthältSuchtext(filter.getSuche()));
    }

    private static Specification<Todo> hatStatus(String status) {
        if (!StringUtils.hasText(status)) return Specification.unrestricted();
        return switch (status) {
            case "offen" -> (root, query, cb) -> cb.equal(root.get("erledigt"), false);
            case "erledigt" -> (root, query, cb) -> cb.equal(root.get("erledigt"), true);
            case "ueberfaellig" -> (root, query, cb) -> cb.and(
                    cb.lessThan(root.get("faelligAm"), LocalDate.now()),
                    cb.equal(root.get("erledigt"), false)
            );
            case "heute" -> (root, query, cb) -> cb.and(
                    cb.equal(root.get("faelligAm"), LocalDate.now()),
                    cb.equal(root.get("erledigt"), false)
            );
            default -> Specification.unrestricted();
        };
    }

    private static Specification<Todo> enthältSuchtext(String suche) {
        if (!StringUtils.hasText(suche)) return Specification.unrestricted();
        String muster = "%" + suche.toLowerCase() + "%";
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("titel")), muster);
    }
}
