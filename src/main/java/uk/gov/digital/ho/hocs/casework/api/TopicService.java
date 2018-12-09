package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoTopic;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicDataRepository;

import javax.transaction.Transactional;
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

    @Transactional
    public Set<Topic> getTopics(UUID caseUUID) {
        Set<Topic> topics = topicDataRepository.findAllByCaseUUID(caseUUID);
        if (topics != null) {
            log.info("Got {} Topics for Case: {}", topics.size(), caseUUID, value(EVENT, CASE_TOPICS_RETRIEVED));
            return topics;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Topics for Case UUID: %s not found!", caseUUID), CASE_TOPICS_NOT_FOUND);
        }
    }

    @Transactional
    public Topic getTopic(UUID caseUUID, UUID topicUUID) {
        Topic topic = topicDataRepository.findByUUID(caseUUID, topicUUID);
        if (topic != null) {
            log.info("Got Topic: {} for Case: {}", topicUUID, caseUUID, value(EVENT, CASE_TOPIC_RETRIEVED));
            return topic;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Topic %s not found for Case: %s", topicUUID, caseUUID), CASE_TOPIC_NOT_FOUND);
        }
    }

    @Transactional
    public Topic getPrimaryTopic(UUID caseUUID) {
        Topic topic = topicDataRepository.getPrimaryTopic(caseUUID);
        if (topic != null) {
            log.info("Got Primary Topic: {} for Case: {}", topic.getUuid(), caseUUID, value(EVENT, CASE_PRIMARY_TOPIC_RETRIEVED));
            return topic;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Primary Topic not found for Case: %s", caseUUID), CASE_PRIMARY_TOPIC_NOT_FOUND);
        }
    }

    @Transactional
    public void createTopic(UUID caseUUID, UUID topicUUID) {
        if (topicUUID != null) {
            InfoTopic infoTopic = infoClient.getTopic(topicUUID);
            Topic topic = new Topic(caseUUID, infoTopic.getLabel(), topicUUID);
            topicDataRepository.save(topic);
            log.info("Created Topic: {} for Case: {}", topic.getUuid(), caseUUID, value(EVENT, CASE_TOPIC_CREATE));
        } else {
            throw new ApplicationExceptions.EntityCreationException(String.format("No TopicUUID given for Case: %s", caseUUID), CASE_TOPIC_UUID_NOT_GIVEN);
        }
    }

    @Transactional
    public void deleteTopic(UUID caseUUID, UUID topicUUID) {
        topicDataRepository.deleteTopic(topicUUID);
        log.info("Deleted Topic: {} for Case: {}", topicUUID, caseUUID, value(EVENT, CASE_TOPIC_DELETED));
    }
}