package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseNotesResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class CaseNoteDataResource {

    private final CaseNoteDataService caseNoteDataService;

    @Autowired
    public CaseNoteDataResource(CaseNoteDataService caseNoteDataService) {
        this.caseNoteDataService = caseNoteDataService;
    }

    @GetMapping(value = "/case/{caseUUID}/casenote", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCaseNotesResponse> getCaseNotesForCase(@PathVariable UUID caseUUID) {
        Set<CaseNoteData> caseNoteData = caseNoteDataService.getCaseNote(caseUUID);
        return ResponseEntity.ok(GetCaseNotesResponse.from(caseNoteData));
    }
}
