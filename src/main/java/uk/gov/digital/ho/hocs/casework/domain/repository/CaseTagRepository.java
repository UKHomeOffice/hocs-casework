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
    @Query("UPDATE CaseDataTag caseDataTag SET caseDataTag.deletedOnDate= CURRENT_TIMESTAMP WHERE caseDataTag.caseUuid = ?1 AND caseDataTag.tag = ?2")
    void deleteByCaseUuidAndTag(UUID caseUuid, String tag);

    @Modifying
    CaseDataTag save(CaseDataTag entity);

    CaseDataTag findByCaseUuidAndTag(UUID caseUuid, String tag);

    Set<CaseDataTag> findAllByCaseUuidIn(Set<UUID> caseUuids);

}
