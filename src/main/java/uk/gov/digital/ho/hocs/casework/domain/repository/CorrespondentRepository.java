package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseCorrespondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CorrespondentRepository extends CrudRepository<BaseCorrespondent, Long> {

    @Query(value = "SELECT aco.* FROM active_correspondent aco WHERE aco.case_uuid = ?1 AND aco.uuid = ?2", nativeQuery = true)
    Correspondent findByUUID(UUID caseUUID, UUID correspondentUUID);

    @Query(value =
                    "SELECT aco.*, " +
                    "CASE " +
                        "WHEN aco.uuid = pc.uuid THEN 1 " +
                        "ELSE 0 " +
                    "END as is_primary " +
                    "FROM active_correspondent aco " +
                    "LEFT JOIN primary_correspondent pc ON aco.case_uuid = pc.case_uuid " +
                    "WHERE aco.case_uuid = ?1", nativeQuery = true
    )
    Set<CorrespondentWithPrimaryFlag> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT aco.* FROM active_correspondent aco", nativeQuery = true)
    Set<Correspondent> findAllActive();
}
