package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.ProfileDto;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
class CaseProfileResource {

    private final CaseDataService caseDataService;
    private final InfoClient infoClient;

    @Autowired
    public CaseProfileResource(@Qualifier("CaseDataService") CaseDataService caseDataService, InfoClient infoClient) {
        this.caseDataService = caseDataService;
        this.infoClient = infoClient;
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY, permittedLowerLevels = {AccessLevel.RESTRICTED_OWNER})
    @GetMapping(value = "/case/profile/{caseUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ProfileDto> getProfileForCase(@PathVariable UUID caseUUID) {
        String caseType = caseDataService.getCaseType(caseUUID);
        ProfileDto profile = infoClient.getProfileByCaseType(caseType);
        return ResponseEntity.ok(profile);
    }

}
