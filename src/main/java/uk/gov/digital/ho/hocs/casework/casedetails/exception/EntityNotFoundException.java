package uk.gov.digital.ho.hocs.casework.casedetails.exception;


public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String msg) {
        super(msg);
    }

    public EntityNotFoundException(String msg, Object... args) {
        super(String.format(msg, args));
    }
}
