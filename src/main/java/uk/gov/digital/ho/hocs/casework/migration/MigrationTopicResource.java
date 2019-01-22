package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateTopicRequest;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@RestController
public class MigrationTopicResource {

    private final MigrationTopicService migrationTopicService;

    @Autowired
    public MigrationTopicResource(MigrationTopicService migrationTopicService) {
        this.migrationTopicService = migrationTopicService;
    }

    @PostMapping(value = "/migration/case/{caseUUID}/stage/{stageUUID}/topic")
    ResponseEntity<UUID> addTopicToCase(@PathVariable UUID caseUUID, @PathVariable UUID stageUUID, @Valid @RequestBody CreateTopicRequest request) {
        UUID topicUUID = migrationTopicService.createTopic(caseUUID, request.getTopicUUID());
        return ResponseEntity.ok(topicUUID);
    }

}