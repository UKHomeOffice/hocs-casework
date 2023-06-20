package uk.gov.digital.ho.hocs.casework.migration.api.exception;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.DuplicateMigratedReferenceResponseDto;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class MigrationRestResponseEntityExceptionHandlerTest {

    private MigrationRestResponseEntityExceptionHandler restResponseEntityExceptionHandler;

    ListAppender<ILoggingEvent> logMessages;

    @Before
    public void beforeTest() {
        restResponseEntityExceptionHandler = new MigrationRestResponseEntityExceptionHandler();

        Logger exceptionLogger = (Logger) LoggerFactory.getLogger(MigrationRestResponseEntityExceptionHandler.class);
        exceptionLogger.detachAndStopAllAppenders();

        logMessages = new ListAppender<>();
        logMessages.start();

        exceptionLogger.addAppender(logMessages);
    }

    @Test
    public void canHandleDuplicateMigratedReferenceException() {
        final UUID existingCaseUUID = UUID.randomUUID();
        final String migratedReference = "MigratedReference";

        MigrationExceptions.DuplicateMigratedReferenceException testException = new MigrationExceptions.DuplicateMigratedReferenceException(
            migratedReference, existingCaseUUID);

        String expectedFormattedMessage = "MigrationExceptions.DuplicateMigratedReferenceException:" +
            " Existing case with migrated reference MigratedReference found," +
            " existing case UUID: " + existingCaseUUID;

        ResponseEntity<DuplicateMigratedReferenceResponseDto> response = restResponseEntityExceptionHandler.handle(
            testException);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMigratedReference()).isEqualTo(migratedReference);
        assertThat(response.getBody().getExistingCaseId()).isEqualTo(existingCaseUUID);

        assertThat(logMessages.list.get(0).getFormattedMessage()).isEqualTo(expectedFormattedMessage);
    }

}
