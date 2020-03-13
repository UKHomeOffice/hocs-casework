package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoTopic;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class TopicService {

    private final TopicRepository topicRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;

    @Autowired
    public TopicService(TopicRepository topicRepository, InfoClient infoClient, AuditClient auditClient) {
        this.topicRepository = topicRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

     Set<Topic> getTopics(UUID caseUUID) {
        log.debug("Getting all Topics for Case: {}", caseUUID);
        Set<Topic> topics = topicRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} Topics for Case: {}", topics.size(), caseUUID, value(EVENT, CASE_TOPICS_RETRIEVED));
        return topics;
    }

    Topic getTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Getting Topic: {} for Case: {}", topicUUID, caseUUID);
        Topic topic = topicRepository.findByUUID(caseUUID, topicUUID);
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
            topicRepository.save(topic);
            auditClient.createTopicAudit(topic);
            log.info("Created Topic: {} for Case: {}", topic.getUuid(), caseUUID, value(EVENT, CASE_TOPIC_CREATE));
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("No TopicUUID given for Case: %s", caseUUID), CASE_TOPIC_UUID_NOT_GIVEN);
        }
    }

    void deleteTopic(UUID caseUUID, UUID topicUUID) {
        log.debug("Deleting Topic: {}", topicUUID);
        Topic topic = getTopic(caseUUID, topicUUID);
        topic.setDeleted(true);
        topicRepository.save(topic);
        auditClient.deleteTopicAudit(topic);
        log.info("Deleted Topic: {}", caseUUID, value(EVENT, CASE_TOPIC_DELETED));
    }

    void clearCachedStandardLineForTopic(UUID topicUUID) {
        infoClient.clearCachedStandardLineForTopic(topicUUID);
    }

    Set<Topic> getAllTopics() {
        log.debug("Getting all Topics allocated to cases");
        Set<Topic> topics = (Set<Topic>) topicRepository.findAll();
        log.info("Got {} Topics", topics.size(), value(EVENT, ALL_CASE_TOPICS_RETRIEVED));
        return topics;
    }
}
