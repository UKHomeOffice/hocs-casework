package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCorrespondentRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
public class MigrationCorrespondentResource {

    private final MigrationCorrespondentService migrationCorrespondentService;

    @Autowired
    public MigrationCorrespondentResource(MigrationCorrespondentService migrationCorrespondentService) {
        this.migrationCorrespondentService = migrationCorrespondentService;
    }

//    @Allocated(allocatedTo = AllocationLevel.USER)
    @PostMapping(value = "/migration/case/{caseUUID}/stage/{stageUUID}/correspondent")
    ResponseEntity<UUID> addCorrespondentToCase(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @Valid @RequestBody CreateCorrespondentRequest request) {
        Address address = new Address(request.getPostcode(), request.getAddress1(), request.getAddress2(), request.getAddress3(), request.getCountry());
        UUID correspondentUUID = migrationCorrespondentService.createCorrespondent(caseUUID, request.getType(), request.getFullname(), address, request.getTelephone(), request.getEmail(), request.getReference());
        return ResponseEntity.ok(correspondentUUID);
    }

}