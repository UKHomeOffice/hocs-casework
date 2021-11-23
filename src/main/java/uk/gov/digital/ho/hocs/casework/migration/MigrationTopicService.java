package uk.gov.digital.ho.hocs.casework.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoTopic;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicRepository;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class MigrationTopicService {

    private final TopicRepository topicRepository;
    private final InfoClient infoClient;

    @Autowired
    public MigrationTopicService(TopicRepository topicRepository, InfoClient infoClient) {
        this.topicRepository = topicRepository;
        this.infoClient = infoClient;
    }

    UUID createTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Creating Topic of Type: {} for Case: {}", topicUUID, caseUUID);
        Topic topic;
        if (topicUUID != null) {
            InfoTopic infoTopic = infoClient.getTopic(topicUUID);
            topic = new Topic(caseUUID, infoTopic.getLabel(), topicUUID);
            topicRepository.save(topic);
            log.info("Created Topic: {} for Case: {}", topic.getUuid(), caseUUID, value(EVENT, CASE_TOPIC_CREATE));
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("No TopicUUID given for Case: %s", caseUUID), CASE_TOPIC_UUID_NOT_GIVEN);
        }
        return topic.getUuid();
    }

}
