package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageDataRepository extends CrudRepository<StageData, String> {

    @Query(value = "SELECT cd.reference as case_reference, cd.type as case_type, sd.*, dd.date as deadline from stage_data sd JOIN case_data cd on sd.case_uuid = cd.uuid LEFT JOIN deadline_data dd ON dd.case_uuid = sd.case_uuid AND dd.stage = sd.type WHERE sd.uuid = ?1 LIMIT 1", nativeQuery = true)
    StageData findByUuid(UUID uuid);

    @Query(value = "SELECT cd.reference as case_reference, cd.type as case_type, sd.*, dd.date as deadline from stage_data sd JOIN case_data cd on sd.case_uuid = cd.uuid LEFT JOIN deadline_data dd ON dd.case_uuid = sd.case_uuid AND dd.stage = sd.type WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<StageData> findAllByCaseUuid(UUID uuid);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = FALSE WHERE uuid = ?1", nativeQuery = true)
    int setInactive(UUID stageUUID);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = TRUE WHERE uuid = ?1", nativeQuery = true)
    int setActive(UUID stageUUID);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = TRUE, team_uuid = ?2, user_uuid = ?3 WHERE uuid = ?1", nativeQuery = true)
    int allocateToUser(UUID stageUUID, UUID teamUUID, UUID userUUID);

    @Modifying
    @Query(value = "UPDATE stage_data SET active = TRUE, team_uuid = ?2, user_uuid = NULL WHERE uuid = ?1", nativeQuery = true)
    int allocateToTeam(UUID stageUUID, UUID teamUUID);

    @Query(value = "SELECT cd.reference as case_reference, cd.type as case_type, sd.*, dd.date as deadline from stage_data sd JOIN case_data cd on sd.case_uuid = cd.uuid LEFT JOIN deadline_data dd ON dd.case_uuid = sd.case_uuid AND dd.stage = sd.type WHERE sd.active = TRUE", nativeQuery = true)
    Set<StageData> findAllActiveStages();
}