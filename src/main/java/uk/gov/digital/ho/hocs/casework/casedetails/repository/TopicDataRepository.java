package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface TopicDataRepository extends CrudRepository<TopicData, String> {

    Set<TopicData> findAllByCaseUUID(UUID caseUUID);

    TopicData findByCaseUUIDAndTopicUUID(UUID caseUUID, UUID topicUUID);
}
