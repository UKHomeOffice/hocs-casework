package uk.gov.digital.ho.hocs.casework.domain.exception;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.*;
import static junit.framework.TestCase.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class RestResponseEntityExceptionHandlerTest {

    @Mock
    MethodArgumentNotValidException methodArgumentNotValidException;

    private RestResponseEntityExceptionHandler restResponseEntityExceptionHandler;
    ListAppender<ILoggingEvent> logMessages;

    @Before
    public void beforeTest(){
        System.out.println("test");
        restResponseEntityExceptionHandler = new RestResponseEntityExceptionHandler();

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(RestResponseEntityExceptionHandler.class);
        exceptionLogger.detachAndStopAllAppenders();

        logMessages = new ListAppender<>();
        logMessages.start();

        exceptionLogger.addAppender(logMessages);
    }

    @Test
    public void handleHttpClientErrorExceptionUnauthorized(){
        String message = "Test Error message";
        HttpClientErrorException exception = new HttpClientErrorException(UNAUTHORIZED, message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 401", 401, result.getStatusCode().value());
        assertEquals("Error message incorrect", "401 " + message, result.getBody());
    }

    @Test
    public void handleHttpClientErrorExceptionForbidden(){
        String message = "Test Error message";
        HttpClientErrorException exception = new HttpClientErrorException(FORBIDDEN, message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 403", 403, result.getStatusCode().value());
        assertEquals("Error message incorrect", "403 " + message, result.getBody());
    }

    @Test
    public void handleHttpClientErrorExceptionNotFound(){
        String message = "Test Error message";
        HttpClientErrorException exception = new HttpClientErrorException(NOT_FOUND, message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 404", 404, result.getStatusCode().value());
        assertEquals("Error message incorrect", "404 " + message, result.getBody());
    }

    @Test
    public void handleHttpClientErrorExceptionDefault(){
        String message = "Test Error message";
        HttpClientErrorException exception = new HttpClientErrorException(I_AM_A_TEAPOT, message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        // the handler as written returns a 400 status, but includes the status code of the exception which triggered it
        // in the error message
        assertEquals("Http code should be 400", 400, result.getStatusCode().value());
        assertEquals("Error message incorrect", "418 " + message, result.getBody());
    }

    @Test
    public void handleHttpServerErrorExceptionUnauthorized(){
        String message = "Test Error message";
        HttpServerErrorException exception = new HttpServerErrorException(UNAUTHORIZED, message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        // the handler as written returns a 500 status, but includes the status code of the exception which triggered it
        // in the error message
        assertEquals("Http code should be 500", 500, result.getStatusCode().value());
        assertEquals("Error message incorrect", "401 " + message, result.getBody());
    }

    @Test
    public void handleEntityCreationException(){
        String message = "Test Error message";


        ApplicationExceptions.EntityCreationException exception =
                new ApplicationExceptions.EntityCreationException(message, LogEvent.AUDIT_EVENT_CREATED);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 500", 500, result.getStatusCode().value());
        assertEquals("Error message incorrect", message, result.getBody());
    }

    @Test
    public void handleEntityNotFoundException(){
        String message = "Test Error message";


        ApplicationExceptions.EntityNotFoundException exception =
                new ApplicationExceptions.EntityNotFoundException(message, LogEvent.AUDIT_EVENT_CREATED);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 404", 404, result.getStatusCode().value());
        assertEquals("Error message incorrect", message, result.getBody());
    }

    @Test
    public void handleMethodArgumentNotValidException(){
        when(methodArgumentNotValidException.getMessage()).thenReturn("Validation failed");

        ResponseEntity result = restResponseEntityExceptionHandler.handle(methodArgumentNotValidException);

        assertEquals("Http code should be 400", 400, result.getStatusCode().value());
        assertEquals("Error message incorrect", "Validation failed", result.getBody());
    }

    @Test
    public void handleHttpMessageConversionException(){
        String message = "Test Error message";


        HttpMessageConversionException exception = new HttpMessageConversionException(message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 400", 400, result.getStatusCode().value());
        assertEquals("Error message incorrect", message, result.getBody());
    }

    @Test
    public void handleHttpMessageUncaughtExceptionResponse(){
        String message = "Test Error message";

        Exception exception = new Exception(message);

        ResponseEntity result = restResponseEntityExceptionHandler.handle(exception);

        assertEquals("Http code should be 500", 500, result.getStatusCode().value());
        assertEquals("Error message incorrect", message, result.getBody());
    }

    @Test
    public void handleHttpMessageUncaughtExceptionLogMessage(){
        String message = "Test Error message";

        Exception exception =
                new Exception(message);

        restResponseEntityExceptionHandler.handle(exception);

        // message format is correct
        assertEquals(logMessages.list.get(0).getMessage(), "Exception: {}, Event: {}, Stack: {}");

        // beginning of stack trace is present
        assertThat(logMessages.list.get(0).getFormattedMessage(), containsString("Stack: java.lang.Exception: Test Error message"));
    }
}

