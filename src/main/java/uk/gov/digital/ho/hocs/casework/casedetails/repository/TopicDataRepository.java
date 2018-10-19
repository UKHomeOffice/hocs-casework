package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Topic;

import java.util.Set;
import java.util.UUID;

@Repository
public interface TopicDataRepository extends CrudRepository<Topic, String> {

    Set<Topic> findAllByCaseUUID(UUID caseUUID);

    Topic findByCaseUUIDAndTopicUUID(UUID caseUUID, UUID topicUUID);
}
