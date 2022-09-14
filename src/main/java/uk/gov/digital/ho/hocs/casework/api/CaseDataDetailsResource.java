package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataDetailsDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

@RestController
class CaseDataDetailsResource {

    private final CaseDataDetailsService caseDataDetailsService;

    public CaseDataDetailsResource(CaseDataDetailsService caseDataDetailsService) {
        this.caseDataDetailsService = caseDataDetailsService;
    }

    @Authorised(accessLevel = AccessLevel.READ, permittedLowerLevels = { AccessLevel.RESTRICTED_OWNER })
    @GetMapping("/case/{caseUUID}/details")
    public ResponseEntity<CaseDataDetailsDto> getCaseDataDetails(@PathVariable UUID caseUUID) {
        return ResponseEntity.ok(caseDataDetailsService.getCaseDataDetails(caseUUID));
    }

}
