package de.teamplaner.service;

import de.teamplaner.config.OrgContext;
import de.teamplaner.dto.TodoFilterDTO;
import de.teamplaner.exception.EntityNotFoundException;
import de.teamplaner.model.Todo;
import de.teamplaner.repository.TodoRepository;
import de.teamplaner.specification.TodoSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TodoService {

    private static final Logger log = LoggerFactory.getLogger(TodoService.class);

    private final TodoRepository todoRepository;
    private final OrgContext orgContext;
    private final AuditService auditService;

    private Specification<Todo> byOrg() {
        Long orgId = orgContext.getOrgId();
        return (root, query, cb) -> cb.equal(root.get("organisation").get("id"), orgId);
    }

    public List<Todo> alle() {
        return todoRepository.findAll(byOrg());
    }

    private static final Sort STANDARD_SORT =
            Sort.by("prioritaet").ascending().and(Sort.by("faelligAm").ascending());

    public List<Todo> mitFilter(TodoFilterDTO filter) {
        return todoRepository.findAll(byOrg().and(TodoSpecification.withFilter(filter)), STANDARD_SORT);
    }

    public List<Todo> alleOffen() {
        Specification<Todo> spec = byOrg().and(
                (root, query, cb) -> cb.isFalse(root.get("erledigt")));
        return todoRepository.findAll(spec, STANDARD_SORT).stream().toList();
    }

    public List<Todo> alleErledigt() {
        Specification<Todo> spec = byOrg().and(
                (root, query, cb) -> cb.isTrue(root.get("erledigt")));
        return todoRepository.findAll(spec);
    }

    public Optional<Todo> findById(Long id) {
        return todoRepository.findByIdAndOrganisationId(id, orgContext.getOrgId());
    }

    public Todo findByIdOrThrow(Long id) {
        return todoRepository.findByIdAndOrganisationId(id, orgContext.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Todo", id));
    }

    @Transactional
    public Todo speichern(Todo todo) {
        boolean isNeu = todo.getId() == null;
        if (isNeu) {
            todo.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Todo: {}", todo.getTitel());
        Todo gespeichert = todoRepository.save(todo);
        auditService.log("Todo", gespeichert.getId(), isNeu ? "CREATE" : "UPDATE");
        return gespeichert;
    }

    @Transactional
    public Todo erledigtToggle(Long id) {
        Todo todo = findByIdOrThrow(id);
        todo.setErledigt(!todo.isErledigt());
        todo.setErledigtAm(todo.isErledigt() ? LocalDateTime.now() : null);
        Todo gespeichert = todoRepository.save(todo);
        auditService.log("Todo", id, "UPDATE");
        return gespeichert;
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Todo mit ID {}", id);
        todoRepository.deleteById(id);
        auditService.log("Todo", id, "DELETE");
    }

    public boolean isUeberfaellig(Todo todo) {
        return todo.getFaelligAm() != null
                && todo.getFaelligAm().isBefore(LocalDate.now())
                && !todo.isErledigt();
    }

    public List<Todo> heuteFaellig() {
        Specification<Todo> spec = byOrg().and(
                (root, query, cb) -> cb.and(
                        cb.equal(root.get("faelligAm"), LocalDate.now()),
                        cb.isFalse(root.get("erledigt"))));
        return todoRepository.findAll(spec);
    }

    public long anzahlOffen() {
        return todoRepository.countByOrganisationIdAndErledigtFalse(orgContext.getOrgId());
    }

    public long anzahlUeberfaellig() {
        return todoRepository.countByOrganisationIdAndFaelligAmBeforeAndErledigtFalse(
                orgContext.getOrgId(), LocalDate.now());
    }

    public long anzahlHeuteFaellig() {
        return todoRepository.countByFaelligAmAndErledigtFalse(LocalDate.now());
    }
}
