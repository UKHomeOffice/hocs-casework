package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseNotesResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNote;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class CaseNoteResource {

    private final CaseNoteService caseNoteService;

    @Autowired
    public CaseNoteResource(CaseNoteService caseNoteService) {
        this.caseNoteService = caseNoteService;
    }

    @GetMapping(value = "/case/{caseUUID}/note", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCaseNotesResponse> getCaseNotesForCase(@PathVariable UUID caseUUID) {
        Set<CaseNote> caseNoteData = caseNoteService.getCaseNotesForCase(caseUUID);
        return ResponseEntity.ok(GetCaseNotesResponse.from(caseNoteData));
    }
}
