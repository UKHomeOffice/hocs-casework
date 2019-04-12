package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
public class MigrationCaseNoteResource {

    private final MigrationCaseNoteService migrationCaseNoteService;

    @Autowired
    public MigrationCaseNoteResource(MigrationCaseNoteService migrationCaseNoteService) {
        this.migrationCaseNoteService = migrationCaseNoteService;
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PostMapping(value = "/migration/case/{caseUUID}/note")
    public ResponseEntity<UUID> migrateCaseNote(@PathVariable UUID caseUUID,@Valid @RequestBody MigrationCreateCaseNoteRequest request) {
        UUID uuid = migrationCaseNoteService.migrationCaseNote(caseUUID, request.getDate(), request.getUser(), request.getText());
        return ResponseEntity.ok(uuid);
    }
}
