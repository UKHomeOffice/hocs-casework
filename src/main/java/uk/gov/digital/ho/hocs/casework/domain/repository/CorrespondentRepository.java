package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CorrespondentRepository extends CrudRepository<Correspondent, Long> {

    @Query(value = "SELECT aco.* FROM active_correspondent aco WHERE aco.case_uuid = ?1 AND aco.uuid = ?2", nativeQuery = true)
    Correspondent findByUUID(UUID caseUUID, UUID correspondentUUID);

    @Query(value = "SELECT aco.* FROM active_correspondent aco WHERE aco.case_uuid = ?1", nativeQuery = true)
    Set<Correspondent> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT pco.* FROM primary_correspondent pco WHERE pco.case_uuid = ?1", nativeQuery = true)
    Correspondent getPrimaryCorrespondent(UUID caseUUID);

    @Modifying
    @Query(value = "UPDATE correspondent c SET c.deleted = TRUE WHERE c.uuid = ?1", nativeQuery = true)
    int deleteCorrespondent(UUID correspondentUUID);
}