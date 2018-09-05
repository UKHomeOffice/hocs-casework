package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class CorrespondentDataResource {

    private final CorrespondentDataService correspondentDataService;

    @Autowired
    public CorrespondentDataResource(CorrespondentDataService correspondentDataService) {
        this.correspondentDataService = correspondentDataService;
    }

    @GetMapping(value = "/case/{caseUUID}/correspondent", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetCorrespondentResponse> getCorrespondents(@PathVariable UUID caseUUID) {
        Set<CorrespondentData> correspondents = correspondentDataService.getCorrespondents(caseUUID);
        return ResponseEntity.ok(GetCorrespondentResponse.from(correspondents));
    }
}
