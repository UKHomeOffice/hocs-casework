package uk.gov.digital.ho.hocs.casework.domain.exception;


public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}