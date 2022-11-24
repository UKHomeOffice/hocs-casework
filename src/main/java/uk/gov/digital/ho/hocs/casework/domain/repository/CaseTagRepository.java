package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataTag;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseTagRepository extends org.springframework.data.repository.Repository<CaseDataTag, UUID> {

    @Modifying
    @Query("UPDATE CaseDataTag caseDataTag SET deleted_on = CURRENT_TIMESTAMP WHERE case_uuid = ?1 AND tag = ?2")
    void deleteByCaseUuidAndTag(UUID caseUuid, String tag);

    @Modifying
    CaseDataTag save(CaseDataTag entity);

    Set<CaseDataTag> findAllByCaseUuidIn(Set<UUID> caseUuids);

}
