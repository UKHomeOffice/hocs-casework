package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@ControllerAdvice
@Slf4j
@Order(HIGHEST_PRECEDENCE)
public class RestResponseSecurityExceptionHandler {

    @ExceptionHandler(SecurityExceptions.PermissionCheckException.class)
    public ResponseEntity handle(SecurityExceptions.PermissionCheckException e) {
        logError(e);
        return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToLoggedInUserException.class)
    public ResponseEntity handle(SecurityExceptions.StageNotAssignedToLoggedInUserException e) {
        logError(e);
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    @ExceptionHandler(SecurityExceptions.StageNotAssignedToUserTeamException.class)
    public ResponseEntity handle(SecurityExceptions.StageNotAssignedToUserTeamException e) {
        logError(e);
        return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
    }

    private void logError(SecurityExceptions.EventRuntimeException e) {
        log.error("SecurityException: {}", e.getMessage(), value(EVENT, e.getEvent()));
    }

}
