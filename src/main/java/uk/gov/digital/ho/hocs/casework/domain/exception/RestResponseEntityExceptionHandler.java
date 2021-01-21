package uk.gov.digital.ho.hocs.casework.domain.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity handle(HttpClientErrorException e) {
        String message = "HttpClientErrorException: {}";
        switch(e.getStatusCode()) {
            case UNAUTHORIZED:
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_UNAUTHORIZED));
                return new ResponseEntity<>(e.getMessage(), UNAUTHORIZED);
            case FORBIDDEN:
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_FORBIDDEN));
                return new ResponseEntity<>(e.getMessage(), FORBIDDEN);
            case NOT_FOUND:
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_NOT_FOUND));
                return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
            default:
                log.error(message, e.getMessage(), value(EVENT, REST_HELPER_GET_BAD_REQUEST));
                return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
        }
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage(),value(EVENT, REST_HELPER_POST_FAILURE));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.EntityCreationException.class)
    public ResponseEntity handle(ApplicationExceptions.EntityCreationException e) {
        log.error("ApplicationExceptions.EntityCreationException: {}", e.getMessage(),value(EVENT, e.getEvent()), value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.EntityNotFoundException.class)
    public ResponseEntity handle(ApplicationExceptions.EntityNotFoundException e) {
        log.error("ApplicationExceptions.EntityNotFoundException: {}", e.getMessage(),value(EVENT, e.getEvent()), value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handle(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(),value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity handle(HttpMessageConversionException e) {
        log.error("HttpMessageConversionException: {}", e.getMessage(),value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(),value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity handle(Exception e) {
        Writer stackTraceWriter = new StringWriter();
        e.printStackTrace(new PrintWriter(stackTraceWriter));
        log.error("Exception: {}, Event: {}, Stack: {}", e.getMessage(), value(EVENT, UNCAUGHT_EXCEPTION),
                value(STACKTRACE, stackTraceWriter.toString()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
