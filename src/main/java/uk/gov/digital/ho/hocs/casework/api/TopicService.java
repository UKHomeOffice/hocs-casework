package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class TopicService {

    private final TopicDataRepository topicDataRepository;

    @Autowired
    public TopicService(TopicDataRepository topicDataRepository) {
        this.topicDataRepository = topicDataRepository;
    }

    @Transactional
    public Set<Topic> getTopics(UUID caseUUID) {
        Set<Topic> topics = topicDataRepository.findAllByCaseUUID(caseUUID);
        if (topics != null) {
            log.info("Got {} Topics for Case: {}", topics.size(), caseUUID);
            return topics;
        } else {
            throw new EntityNotFoundException("Topics for Case UUID: %s not found!", caseUUID);
        }
    }

    @Transactional
    public Topic getTopic(UUID caseUUID, UUID topicUUID) {
        Topic topic = topicDataRepository.findByUUID(caseUUID, topicUUID);
        if (topic != null) {
            log.info("Got Topic: {} for Case: {}", topicUUID, caseUUID);
            return topic;
        } else {
            throw new EntityNotFoundException("Topic %s not found for Case: %s", topicUUID, caseUUID);
        }
    }

    @Transactional
    public Topic getPrimaryTopic(UUID caseUUID) {
        Topic topic = topicDataRepository.getPrimaryTopic(caseUUID);
        if (topic != null) {
            log.info("Got Primary Topic: {} for Case: {}", topic.getUuid(), caseUUID);
            return topic;
        } else {
            throw new EntityNotFoundException("Primary Topic not found for Case: %s", caseUUID);
        }
    }

    @Transactional
    public void createTopic(UUID caseUUID, String text, UUID topicUUID) {
        Topic topic = new Topic(caseUUID, text, topicUUID);
        topicDataRepository.save(topic);
        log.info("Created Topic: {} for Case: {}", topic.getUuid(), caseUUID);
    }

    @Transactional
    public void deleteTopic(UUID caseUUID, UUID topicUUID) {
        topicDataRepository.deleteTopic(topicUUID);
        log.info("Deleted Topic: {} for Case: {}", topicUUID, caseUUID);
    }
}