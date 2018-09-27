package uk.gov.digital.ho.hocs.casework.casedetails;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;
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

    public Set<TopicData> getCaseTopics(UUID caseUUID) {
        log.debug("Getting Topics for case {}", caseUUID);
        Set<TopicData> topics = topicDataRepository.findAllByCaseUUID(caseUUID);
        return topics;
    }

    public TopicData addTopicToCase(UUID caseUUID, UUID topicUUID, String topicName) {
        log.debug("Adding Topic to case {}", caseUUID);
        TopicData topicData = topicDataRepository.findByCaseUUIDAndTopicUUID(caseUUID,topicUUID);
        if(topicData != null){
            topicData.reAdd();
        } else {
            topicData = new TopicData(caseUUID, topicName, topicUUID);
        }
        topicDataRepository.save(topicData);
        log.info("Added Topic to case {}", caseUUID);
        return topicData;
    }

    public TopicData deleteTopicFromCase(UUID caseUUID, UUID topicUUID) {
        log.debug("Deleting Topic to case {}", caseUUID);
        TopicData topicData = topicDataRepository.findByCaseUUIDAndTopicUUID(caseUUID,topicUUID);
        topicData.delete();
        topicDataRepository.save(topicData);
        log.info("Deleted Topic from case {}", caseUUID);
        return topicData;
    }
}
