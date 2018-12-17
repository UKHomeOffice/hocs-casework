package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.Set;
import java.util.UUID;

@Repository
public interface TopicDataRepository extends CrudRepository<Topic, Long> {

    @Query(value = "SELECT ato.* FROM active_topic ato WHERE ato.case_uuid = ?1", nativeQuery = true)
    Set<Topic> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT ato.* FROM active_topic ato WHERE ato.case_uuid = ?1 AND ato.uuid = ?2", nativeQuery = true)
    Topic findByUUID(UUID caseUUID, UUID topicUUID);

    @Modifying
    @Query(value = "UPDATE topic SET deleted = TRUE WHERE uuid = ?1", nativeQuery = true)
    int deleteTopic(UUID topicUUID);
}