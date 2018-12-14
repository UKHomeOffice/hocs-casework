package uk.gov.digital.ho.hocs.casework.domain.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.UNCAUGHT_EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(ApplicationExceptions.EntityCreationException.class)
    public ResponseEntity handle(ApplicationExceptions.EntityCreationException e) {
        log.error("ApplicationExceptions.EntityCreationException: {}", e.getMessage(),value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.EntityNotFoundException.class)
    public ResponseEntity handle(ApplicationExceptions.EntityNotFoundException e) {
        log.error("ApplicationExceptions.EntityNotFoundException: {}", e.getMessage(),value(EVENT, e.getEvent()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(),value(EVENT, UNCAUGHT_EXCEPTION));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handle(Exception e) {
        log.error("Exception: {}", e.getMessage(), value(EVENT, UNCAUGHT_EXCEPTION));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
