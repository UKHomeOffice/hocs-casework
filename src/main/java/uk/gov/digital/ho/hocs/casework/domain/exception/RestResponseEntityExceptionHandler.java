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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.DATA_MAPPING_EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EXCEPTION;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REST_HELPER_GET_BAD_REQUEST;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REST_HELPER_GET_FORBIDDEN;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REST_HELPER_GET_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REST_HELPER_GET_UNAUTHORIZED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REST_HELPER_POST_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.UNCAUGHT_EXCEPTION;

@ControllerAdvice
@Slf4j
public class RestResponseEntityExceptionHandler {

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<String> handle(HttpClientErrorException e) {
        String message = "HttpClientErrorException: {}";
        switch (e.getStatusCode()) {
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
    public ResponseEntity<String> handle(HttpServerErrorException e) {
        log.error("HttpServerErrorException: {}", e.getMessage(), value(EVENT, REST_HELPER_POST_FAILURE));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.EntityCreationException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.EntityCreationException e) {
        log.error("ApplicationExceptions.EntityCreationException: {}", e.getMessage(), value(EVENT, e.getEvent()),
            value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.EntityNotFoundException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.EntityNotFoundException e) {
        log.error("ApplicationExceptions.EntityNotFoundException: {}", e.getMessage(), value(EVENT, e.getEvent()),
            value(EXCEPTION, e.getException()));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handle(MethodArgumentNotValidException e) {
        log.error("MethodArgumentNotValidException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    public ResponseEntity<String> handle(HttpMessageConversionException e) {
        log.error("HttpMessageConversionException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handle(HttpMessageNotReadableException e) {
        log.error("HttpMessageNotReadableException: {}", e.getMessage(), value(EVENT, BAD_REQUEST));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<String> handle(UnsupportedOperationException e) {
        log.error("UnsupportedOperationException: {}", e.getMessage(), value(EVENT, METHOD_NOT_ALLOWED));
        return new ResponseEntity<>(e.getMessage(), METHOD_NOT_ALLOWED);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> handle(MethodArgumentTypeMismatchException e) {
        log.error("ConversionFailedException: {}", e.getMessage(), value(EVENT, METHOD_NOT_ALLOWED));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationExceptions.DataMappingException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.DataMappingException e) {
        log.error("DataMappingException: {}", e.getMessage(), value(EVENT, DATA_MAPPING_EXCEPTION));
        return new ResponseEntity<>(e.getMessage(), BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationExceptions.TeamAllocationException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.TeamAllocationException e) {
        log.error("TeamAllocationException: {}", e.getMessage(), value(EVENT, INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.DatabaseConflictException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.DatabaseConflictException e) {
        log.error("DatabaseConflictException: {}", e.getMessage(), value(EVENT, CONFLICT));
        return new ResponseEntity<>(e.getMessage(), CONFLICT);
    }

    @ExceptionHandler(ApplicationExceptions.ReportBodyStreamingException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.ReportBodyStreamingException e) {
        log.error("ReportBodyStreamingException: {}", e.getMessage(), value(EVENT, INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(ApplicationExceptions.ReportCaseTypeNotSupportedException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.ReportCaseTypeNotSupportedException e) {
        log.warn("ReportCaseTypeNotSupportedException: {}", e.getMessage(), value(EVENT, NOT_FOUND));
        return new ResponseEntity<>(e.getMessage(), NOT_FOUND);
    }

    @ExceptionHandler(ApplicationExceptions.StreamingResponseBodyException.class)
    public ResponseEntity<String> handle(ApplicationExceptions.StreamingResponseBodyException e) {
        log.error("StreamingResponseBodyException: {}", e.getMessage(), value(EVENT, INTERNAL_SERVER_ERROR));
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handle(Exception e) {
        Writer stackTraceWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stackTraceWriter);
        e.printStackTrace(printWriter);
        log.error("Exception: {}, Event: {}, Stack: {}", e.getMessage(), value(EVENT, UNCAUGHT_EXCEPTION),
            stackTraceWriter);
        return new ResponseEntity<>(e.getMessage(), INTERNAL_SERVER_ERROR);
    }

}
