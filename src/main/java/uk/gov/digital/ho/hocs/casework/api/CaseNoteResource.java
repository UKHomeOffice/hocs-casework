package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseNoteRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNoteResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseNotesResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import jakarta.validation.Valid;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @Autowired
    public CaseNoteResource(CaseNoteService caseNoteService) {
        this.caseNoteService = caseNoteService;
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/note")
    ResponseEntity<GetCaseNotesResponse> getCaseNotesForCase(@PathVariable UUID caseUUID) {
        Set<CaseNote> caseNoteData = caseNoteService.getCaseNotes(caseUUID);
        return ResponseEntity.ok(GetCaseNotesResponse.from(caseNoteData));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/note/{noteUUID}")
    ResponseEntity<GetCaseNoteResponse> getCaseNote(@PathVariable UUID caseUUID, @PathVariable UUID noteUUID) {
        CaseNote caseNoteData = caseNoteService.getCaseNote(noteUUID);
        return ResponseEntity.ok(GetCaseNoteResponse.from(caseNoteData));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PostMapping(value = "/case/{caseUUID}/note")
    public ResponseEntity<UUID> createCaseNote(@PathVariable UUID caseUUID,
                                               @Valid @RequestBody CreateCaseNoteRequest createCaseNoteRequest) {
        CaseNote caseNote = caseNoteService.createCaseNote(caseUUID, createCaseNoteRequest.getType(),
            createCaseNoteRequest.getText());
        return ResponseEntity.ok(caseNote.getUuid());
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @PutMapping(value = "/case/{caseUUID}/note/{noteUUID}")
    ResponseEntity<GetCaseNoteResponse> updateCaseNote(@PathVariable UUID caseUUID,
                                                       @PathVariable UUID noteUUID,
                                                       @Valid @RequestBody
                                                       CreateCaseNoteRequest createCaseNoteRequest) {
        CaseNote caseNote = caseNoteService.updateCaseNote(noteUUID, createCaseNoteRequest.getType(),
            createCaseNoteRequest.getText());
        return ResponseEntity.ok(GetCaseNoteResponse.from(caseNote));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @DeleteMapping(value = "/case/{caseUUID}/note/{noteUUID}")
    ResponseEntity<UUID> deleteCaseNote(@PathVariable UUID caseUUID, @PathVariable UUID noteUUID) {
        CaseNote caseNote = caseNoteService.deleteCaseNote(noteUUID);
        return ResponseEntity.ok(caseNote.getUuid());
    }

}
