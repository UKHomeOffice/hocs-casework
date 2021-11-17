package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.Set;
import java.util.UUID;

@Repository
public interface TopicRepository extends CrudRepository<Topic, Long> {

    @Query(value = "SELECT t.* FROM topic t JOIN case_data cd ON t.case_uuid = cd.uuid WHERE t.case_uuid = ?1 AND NOT t.deleted AND NOT cd.deleted", nativeQuery = true)
    Set<Topic> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT t.* FROM topic t JOIN case_data cd ON t.case_uuid = cd.uuid WHERE t.case_uuid = ?1 AND t.uuid = ?2 AND NOT t.deleted AND NOT cd.deleted", nativeQuery = true)
    Topic findByUUID(UUID caseUUID, UUID topicUUID);

    @Query(value = "SELECT t.* FROM topic t JOIN case_data cd ON t.case_uuid = cd.uuid WHERE NOT t.deleted AND NOT cd.deleted", nativeQuery = true)
    Set<Topic> findAll();
}
