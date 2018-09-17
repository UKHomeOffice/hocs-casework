package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AddTopicToCaseRequest;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class TopicDataResource {

    private final TopicDataService topicDataService;

    @Autowired
    public TopicDataResource(TopicDataService topicDataService) {
        this.topicDataService = topicDataService;
    }

    @PostMapping(value = "/case/{caseUUID}/topic", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addTopicToCase(@PathVariable UUID caseUUID, @RequestBody AddTopicToCaseRequest request) {
        topicDataService.addTopicToCase(caseUUID, request.getTopicUUID());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteTopicFromCase(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        topicDataService.deleteTopicFromCase(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }
}
