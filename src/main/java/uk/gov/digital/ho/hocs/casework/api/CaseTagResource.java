package uk.gov.digital.ho.hocs.casework.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseTagDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseTagRequest;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
class CaseTagResource {

    private final CaseTagService caseTagService;

    public CaseTagResource(CaseTagService caseTagService) {
        this.caseTagService = caseTagService;
    }

    @PostMapping(value = "/case/{caseUuid}/tag", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<CaseTagDto> addTagToCase(@PathVariable UUID caseUuid, @RequestBody CaseTagRequest caseTagRequest) {
        var caseTag = caseTagService.addTagToCase(caseUuid, caseTagRequest.getTag());
        return ResponseEntity.ok(CaseTagDto.from(caseTag));
    }

    @DeleteMapping(value = "/case/{caseUuid}/tag/{tag}")
    public ResponseEntity<Void> removeTagFromCase(@PathVariable UUID caseUuid, @PathVariable String tag) {
        caseTagService.removeTagFromCase(caseUuid, tag);
        return ResponseEntity.noContent().build();
    }

}
