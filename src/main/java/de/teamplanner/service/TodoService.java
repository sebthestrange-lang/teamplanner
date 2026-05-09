package de.teamplanner.service;

import de.teamplanner.config.OrgContext;
import de.teamplanner.dto.TodoFilterDTO;
import de.teamplanner.exception.EntityNotFoundException;
import de.teamplanner.model.Todo;
import de.teamplanner.repository.TodoRepository;
import de.teamplanner.specification.TodoSpecification;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private Specification<Todo> byOrg() {
        Long orgId = orgContext.getOrgId();
        return (root, query, cb) -> cb.equal(root.get("organisation").get("id"), orgId);
    }

    public List<Todo> alle() {
        return todoRepository.findAll(byOrg());
    }

    public List<Todo> mitFilter(TodoFilterDTO filter) {
        return todoRepository.findAll(byOrg().and(TodoSpecification.withFilter(filter)));
    }

    public List<Todo> alleOffen() {
        Specification<Todo> spec = byOrg().and(
                (root, query, cb) -> cb.isFalse(root.get("erledigt")));
        return todoRepository.findAll(spec,
                org.springframework.data.domain.Sort.by("faelligAm").ascending()).stream().toList();
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
        if (todo.getId() == null) {
            todo.setOrganisation(orgContext.getOrganisation());
        }
        log.debug("Speichere Todo: {}", todo.getTitel());
        return todoRepository.save(todo);
    }

    @Transactional
    public Todo erledigtToggle(Long id) {
        Todo todo = findByIdOrThrow(id);
        todo.setErledigt(!todo.isErledigt());
        todo.setErledigtAm(todo.isErledigt() ? LocalDateTime.now() : null);
        return todoRepository.save(todo);
    }

    @Transactional
    public void loeschen(Long id) {
        findByIdOrThrow(id);
        log.debug("Lösche Todo mit ID {}", id);
        todoRepository.deleteById(id);
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
