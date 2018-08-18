package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.UUID;

@Repository
public interface StageDataRepository extends CrudRepository<StageData, String> {

    StageData findByUuid(UUID uuid);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = FALSE WHERE uuid = ?1", nativeQuery = true)
    int setInactive(UUID stageUUID);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = TRUE AND team_uuid = ?2 AND user_uuid = ?3 WHERE uuid = ?1", nativeQuery = true)
    int allocate(UUID stageUUID, UUID teamUUID, UUID userUUID);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = TRUE AND team_uuid = ?2 WHERE uuid = ?1", nativeQuery = true)
    int allocate(UUID stageUUID, UUID teamUUID);
}