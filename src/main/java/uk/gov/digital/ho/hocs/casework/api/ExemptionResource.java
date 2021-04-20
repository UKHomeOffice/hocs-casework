package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.domain.model.Exemption;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import javax.validation.Valid;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class ExemptionResource {
    @Autowired
    ExemptionService exemptionService;


//    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PostMapping(value = "/case/{caseUUID}/exemption")
    ResponseEntity addExemptionToCase(@PathVariable UUID caseUUID, @Valid @RequestBody CreateExemptionRequest request) {
        exemptionService.createExemption(caseUUID, request.getType());
        return ResponseEntity.ok().build();
    }


    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/exemptions")
    ResponseEntity<GetExemptionsResponse> getExemptions(@PathVariable UUID caseUUID) {
        Set<Exemption> exemptions = exemptionService.getExemptions(caseUUID);
        return ResponseEntity.ok(GetExemptionsResponse.from(exemptions));
    }

}
