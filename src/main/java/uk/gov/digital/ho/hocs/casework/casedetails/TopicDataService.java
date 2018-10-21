package uk.gov.digital.ho.hocs.casework.casedetails;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Topic;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.TopicDataRepository;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class TopicDataService {

    private final TopicDataRepository topicDataRepository;

    @Autowired
    public TopicDataService(TopicDataRepository topicDataRepository) {
        this.topicDataRepository = topicDataRepository;
    }

    public Set<Topic> getCaseTopics(UUID caseUUID) {
        log.debug("Getting Topics for case {}", caseUUID);
        Set<Topic> topics = topicDataRepository.findAllByCaseUUID(caseUUID);
        return topics;
    }

    public Topic addTopicToCase(UUID caseUUID, UUID topicUUID, String topicName) {
        log.debug("Adding Topic to case {}", caseUUID);
        Topic topic = topicDataRepository.findByCaseUUIDAndTopicUUID(caseUUID, topicUUID);
        if (topic != null) {
            //  topic.reAdd();
        } else {
            topic = new Topic(caseUUID, topicName, topicUUID);
        }
        topicDataRepository.save(topic);
        log.info("Added Topic to case {}", caseUUID);
        return topic;
    }

    public Topic deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        log.debug("Deleting Topic to case {}", caseUUID);
        Topic topic = topicDataRepository.findByCaseUUIDAndTopicUUID(caseUUID, topicUUID);
        topic.delete();
        topicDataRepository.save(topic);
        log.info("Deleted Topic from case {}", caseUUID);
        return topic;
    }
}
