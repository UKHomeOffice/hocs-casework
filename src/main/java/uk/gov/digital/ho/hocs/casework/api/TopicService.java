package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoTopic;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicDataRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class TopicService {

    private final TopicDataRepository topicDataRepository;
    private final InfoClient infoClient;

    @Autowired
    public TopicService(TopicDataRepository topicDataRepository, InfoClient infoClient) {
        this.topicDataRepository = topicDataRepository;
        this.infoClient = infoClient;
    }

     Set<Topic> getTopics(UUID caseUUID) {
         log.debug("Getting all Topics for Case: {}", caseUUID);
         Set<Topic> topics = topicDataRepository.findAllByCaseUUID(caseUUID);
        if (topics != null) {
            log.info("Got {} Topics for Case: {}", topics.size(), caseUUID, value(EVENT, CASE_TOPICS_RETRIEVED));
            return topics;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Topics for Case UUID: %s not found!", caseUUID), CASE_TOPICS_NOT_FOUND);
        }
    }

    Topic getTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Getting Topic: {} for Case: {}", topicUUID, caseUUID);
        Topic topic = topicDataRepository.findByUUID(caseUUID, topicUUID);
        if (topic != null) {
            log.info("Got Topic: {} for Case: {}", topicUUID, caseUUID, value(EVENT, CASE_TOPIC_RETRIEVED));
            return topic;
        } else {
            log.error("topic: {} for Case UUID: {} not found!", topicUUID, caseUUID, value(EVENT, CASE_TOPIC_NOT_FOUND));
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Topic %s not found for Case: %s", topicUUID, caseUUID), CASE_TOPIC_NOT_FOUND);
        }
    }

    void createTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Creating Topic of Type: {} for Case: {}", topicUUID, caseUUID);
        if (topicUUID != null) {
            InfoTopic infoTopic = infoClient.getTopic(topicUUID);
            Topic topic = new Topic(caseUUID, infoTopic.getLabel(), topicUUID);
            topicDataRepository.save(topic);
            log.info("Created Topic: {} for Case: {}", topic.getUuid(), caseUUID, value(EVENT, CASE_TOPIC_CREATE));
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("No TopicUUID given for Case: %s", caseUUID), CASE_TOPIC_UUID_NOT_GIVEN);
        }
    }

    void deleteTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Deleting Topic: {} for Case: {}", topicUUID, caseUUID);
        topicDataRepository.deleteTopic(topicUUID);
        log.info("Deleted Topic: {} for Case: {}", topicUUID, caseUUID, value(EVENT, CASE_TOPIC_DELETED));
    }
}