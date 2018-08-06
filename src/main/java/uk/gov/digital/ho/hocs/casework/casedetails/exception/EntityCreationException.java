package uk.gov.digital.ho.hocs.casework.casedetails.exception;

public class EntityCreationException extends RuntimeException {

    public EntityCreationException(String msg) {
        super(msg);
    }

    public EntityCreationException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}