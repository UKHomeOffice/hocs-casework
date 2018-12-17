package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateTopicRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
public class TopicResource {

    private final TopicService topicService;

    @Autowired
    public TopicResource(TopicService topicService) {
        this.topicService = topicService;
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @PostMapping(value = "/case/{caseUUID}/topic")
    ResponseEntity addTopicToCase(@PathVariable UUID caseUUID, @RequestBody CreateTopicRequest request) {
        topicService.createTopic(caseUUID, request.getTopicUUID());
        return ResponseEntity.ok().build();
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/topic")
    ResponseEntity<GetTopicsResponse> getCaseTopics(@PathVariable UUID caseUUID) {
        Set<Topic> topics = topicService.getTopics(caseUUID);
        return ResponseEntity.ok(GetTopicsResponse.from(topics));
    }

    @Authorised(accessLevel = AccessLevel.READ)
    @GetMapping(value = "/case/{caseUUID}/topic/{topicUUID}")
    ResponseEntity<GetTopicResponse> getTopic(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        Topic topic = topicService.getTopic(caseUUID, topicUUID);
        return ResponseEntity.ok(GetTopicResponse.from(topic));
    }

    @Authorised(accessLevel = AccessLevel.WRITE)
    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}")
    ResponseEntity deleteTopic(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        topicService.deleteTopic(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }
}