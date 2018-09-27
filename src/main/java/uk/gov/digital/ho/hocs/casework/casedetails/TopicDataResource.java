package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AddTopicToCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.Set;
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

    @GetMapping(value = "/case/{caseUUID}/topic", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetTopicsResponse> addTopicToCase(@PathVariable UUID caseUUID) {
        Set<TopicData> topics = topicDataService.getCaseTopics(caseUUID);
        return ResponseEntity.ok(GetTopicsResponse.from(topics));
    }

    @PostMapping(value = "/case/{caseUUID}/topic", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity addTopicToCase(@PathVariable UUID caseUUID, @RequestBody AddTopicToCaseRequest request) {
        topicDataService.addTopicToCase(caseUUID, request.getTopicUUID(), request.getTopicName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteTopicFromCase(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        topicDataService.deleteTopicFromCase(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }
}
