package uk.gov.digital.ho.hocs.casework.migration.api.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.DuplicateMigratedReferenceResponseDto;

import static net.logstash.logback.argument.StructuredArguments.value;
import static org.springframework.http.HttpStatus.CONFLICT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.MIGRATION_DUPLICATE_MIGRATED_REFERENCE;

@ControllerAdvice
@Slf4j
public class MigrationRestResponseEntityExceptionHandler {

    @ExceptionHandler(MigrationExceptions.DuplicateMigratedReferenceException.class)
    public ResponseEntity<DuplicateMigratedReferenceResponseDto> handle(MigrationExceptions.DuplicateMigratedReferenceException e) {
        log.warn(
            "MigrationExceptions.DuplicateMigratedReferenceException: {}", e.getMessage(),
            value(EVENT, MIGRATION_DUPLICATE_MIGRATED_REFERENCE));
        return new ResponseEntity<>(DuplicateMigratedReferenceResponseDto.fromException(e), CONFLICT);
    }
}
