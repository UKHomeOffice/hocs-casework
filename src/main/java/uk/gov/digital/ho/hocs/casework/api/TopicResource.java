package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.TopicDto;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.Allocated;
import uk.gov.digital.ho.hocs.casework.security.AllocationLevel;
import uk.gov.digital.ho.hocs.casework.security.Authorised;

import java.util.Set;
import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

@Slf4j
@RestController
public class TopicResource {

    private final TopicService topicService;

    @Autowired
    public TopicResource(TopicService topicService) {
        this.topicService = topicService;
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/topic", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetTopicsResponse> getCaseTopics(@PathVariable UUID caseUUID) {
        Set<Topic> topics = topicService.getTopics(caseUUID);
        return ResponseEntity.ok(GetTopicsResponse.from(topics));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/topic/{topicUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TopicDto> getTopic(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        Topic topic = topicService.getTopic(caseUUID, topicUUID);
        return ResponseEntity.ok(TopicDto.from(topic));
    }

    @Authorised(accessLevel = AccessLevel.SUMMARY)
    @GetMapping(value = "/case/{caseUUID}/topic/primary", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TopicDto> getPrimaryTopic(@PathVariable UUID caseUUID) {
        Topic topic = topicService.getPrimaryTopic(caseUUID);
        return ResponseEntity.ok(TopicDto.from(topic));
    }

    @Allocated(allocatedTo = AllocationLevel.USER)
    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}")
    public ResponseEntity deleteTopic(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        topicService.deleteTopic(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }
}