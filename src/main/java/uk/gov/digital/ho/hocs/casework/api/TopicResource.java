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

    @GetMapping(value = "/case/{caseUUID}/topic", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetTopicsResponse> getCaseTopics(@PathVariable UUID caseUUID) {
        Set<Topic> topics = topicService.getTopics(caseUUID);
        return ResponseEntity.ok(GetTopicsResponse.from(topics));
    }

    @GetMapping(value = "/case/{caseUUID}/topic/{topicUUID}", produces = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TopicDto> getCaseTopics(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        Topic topic = topicService.getTopic(caseUUID, topicUUID);
        return ResponseEntity.ok(TopicDto.from(topic));
    }

    @DeleteMapping(value = "/case/{caseUUID}/topic/{topicUUID}", consumes = APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity deleteTopicFromCase(@PathVariable UUID caseUUID, @PathVariable UUID topicUUID) {
        topicService.deleteTopic(caseUUID, topicUUID);
        return ResponseEntity.ok().build();
    }
}