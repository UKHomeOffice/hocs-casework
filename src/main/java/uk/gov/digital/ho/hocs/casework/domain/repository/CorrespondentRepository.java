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

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid WHERE c.case_uuid = ?1 AND c.uuid = ?2 AND NOT c.deleted AND NOT cd.deleted", nativeQuery = true)
    Correspondent findByUUID(UUID caseUUID, UUID correspondentUUID);

    @Query(value = "SELECT c.*, CASE WHEN c.uuid = cd.primary_correspondent_uuid THEN 1 ELSE 0 END AS is_primary FROM correspondent c LEFT JOIN case_data cd ON c.uuid = cd.primary_correspondent_uuid WHERE c.case_uuid = ?1 AND NOT c.deleted AND NOT cd.deleted", nativeQuery = true)
    Set<CorrespondentWithPrimaryFlag> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT c.* FROM correspondent c JOIN case_data cd on c.case_uuid = cd.uuid AND NOT c.deleted AND NOT cd.deleted", nativeQuery = true)
    Set<Correspondent> findAllActive();
}
