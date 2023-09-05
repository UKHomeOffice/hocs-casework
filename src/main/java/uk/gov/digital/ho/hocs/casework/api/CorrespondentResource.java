package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.CorrespondentTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentOutlinesResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentTypeResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentsResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class CorrespondentResource {

    private final CorrespondentService correspondentService;

    @Autowired
    public CorrespondentResource(CorrespondentService correspondentService) {
        this.correspondentService = correspondentService;
    }

    @GetMapping(value = "/correspondents")
    ResponseEntity<GetCorrespondentOutlinesResponse> getAllActiveCorrespondents(
        @RequestParam(value = "includeDeleted", required = false) Optional<Boolean> includeDeleted) {
        Set<Correspondent> correspondents = correspondentService.getAllCorrespondents(includeDeleted.orElse(false));
        return ResponseEntity.ok(GetCorrespondentOutlinesResponse.from(correspondents));
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PostMapping(value = "/case/{caseUUID}/stage/{stageUUID}/correspondent")
    ResponseEntity addCorrespondentToCase(@PathVariable UUID caseUUID,
                                          @PathVariable UUID stageUUID,
                                          @Valid @RequestBody CreateCorrespondentRequest request) {
        Address address = new Address(request.getPostcode(), request.getAddress1(), request.getAddress2(),
            request.getAddress3(), request.getCountry());
        correspondentService.createCorrespondent(caseUUID, stageUUID, request.getType(), request.getFullname(),
            request.getOrganisation(), address, request.getTelephone(), request.getEmail(), request.getReference(),
            request.getExternalKey());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/correspondent")
    ResponseEntity<GetCorrespondentsResponse> getCorrespondents(@PathVariable UUID caseUUID) {
        Set<CorrespondentWithPrimaryFlag> correspondents = correspondentService.getCorrespondents(caseUUID);
        return ResponseEntity.ok(GetCorrespondentsResponse.from(correspondents));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/correspondent/{correspondentUUID}")
    ResponseEntity<GetCorrespondentResponse> getCorrespondent(@PathVariable UUID caseUUID,
                                                              @PathVariable UUID correspondentUUID) {
        Correspondent correspondent = correspondentService.getCorrespondent(caseUUID, correspondentUUID);
        return ResponseEntity.ok(GetCorrespondentResponse.from(correspondent));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/correspondentType")
    ResponseEntity<GetCorrespondentTypeResponse> getCorrespondentType(@PathVariable UUID caseUUID) {
        Set<CorrespondentTypeDto> correspondentTypes = correspondentService.getCorrespondentTypes(caseUUID);
        return ResponseEntity.ok(GetCorrespondentTypeResponse.from(correspondentTypes));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/correspondentType/selectable")
    ResponseEntity<GetCorrespondentTypeResponse> getSelectableCorrespondentTypes(@PathVariable UUID caseUUID) {
        Set<CorrespondentTypeDto> correspondentTypes = correspondentService.getSelectableCorrespondentTypes(caseUUID);
        return ResponseEntity.ok(GetCorrespondentTypeResponse.from(correspondentTypes));
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @DeleteMapping(value = "/case/{caseUUID}/stage/{stageUUID}/correspondent/{correspondentUUID}")
    ResponseEntity<GetCorrespondentResponse> deleteCorrespondent(@PathVariable UUID caseUUID,
                                                                 @PathVariable UUID stageUUID,
                                                                 @PathVariable UUID correspondentUUID) {
        correspondentService.deleteCorrespondent(caseUUID, stageUUID, correspondentUUID);
        return ResponseEntity.ok().build();
    }

    @Allocated(allocatedTo = AllocationLevel.USER_OR_TEAM)
    @PutMapping(value = "/case/{caseUUID}/stage/{stageUUID}/correspondent/{correspondentUUID}")
    ResponseEntity updateCorrespondent(@PathVariable UUID caseUUID,
                                       @PathVariable UUID stageUUID,
                                       @PathVariable UUID correspondentUUID,
                                       @Valid @RequestBody UpdateCorrespondentRequest request) {
        correspondentService.updateCorrespondent(caseUUID, correspondentUUID, request);
        return ResponseEntity.ok().build();
    }

}
