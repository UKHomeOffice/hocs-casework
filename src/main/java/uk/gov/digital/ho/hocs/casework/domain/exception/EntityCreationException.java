package uk.gov.digital.ho.hocs.casework.domain.exception;

public class EntityCreationException extends RuntimeException {

    public EntityCreationException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}