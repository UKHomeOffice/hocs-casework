package uk.gov.digital.ho.hocs.casework.security;

public class PermissionCheckException extends RuntimeException {
    public PermissionCheckException(String s) {
        super(s);
    }
}
