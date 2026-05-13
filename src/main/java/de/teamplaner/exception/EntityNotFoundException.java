package de.teamplaner.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " mit ID " + id + " nicht gefunden");
    }
}
