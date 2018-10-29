package uk.gov.digital.ho.hocs.casework.security;

public class StageNotAssignedToLoggedInUserException extends RuntimeException {
    public StageNotAssignedToLoggedInUserException(String s) {
        super(s);
    }
}
