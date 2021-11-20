package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.BasicStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageRepository extends CrudRepository<BaseStage, Long> {

    @Query(value = "SELECT s.* FROM stage s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid = ?1 AND s.team_uuid IS NOT NULL AND NOT cd.deleted", nativeQuery = true)
    Set<BasicStage> findAllActiveBasicStageByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT s.* FROM stage s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid = ?1 AND s.uuid = ?2 AND s.team_uuid IS NOT NULL AND NOT cd.deleted", nativeQuery = true)
    BasicStage findActiveBasicStageByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT s.* FROM stage s WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    BasicStage findBasicStageByCaseUuidAndStageUuid(UUID caseUuid, UUID stageUuid);

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findActiveByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid IN ?1", nativeQuery = true)
    Set<Stage> findAllByCaseUUIDIn(Set<UUID> caseUUID);

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.team_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllActiveByTeamUUID(UUID teamUUID);

    @Query(
            value = "SELECT sd.* FROM active_stage_data sd WHERE sd.team_uuid = ?1 AND sd.user_uuid IS NULL",
            nativeQuery = true
    )
    Set<Stage> findAllUnassignedAndActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT * FROM active_stage_data sd WHERE sd.team_uuid IN ?1 OR sd.case_type IN ?2", nativeQuery = true)
    Set<Stage> findAllActiveByTeamUUIDAndCaseType(Set<UUID> teamUUID, Set<String> caseTypes);

    @Query(value = "SELECT * FROM active_stage_data sd WHERE user_uuid = ?1 AND NOT data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) AND (sd.team_uuid IN ?2 OR sd.case_type IN ?3)", nativeQuery = true)
    Set<Stage> findAllActiveByUserUuidAndTeamUuidAndCaseType(UUID userUuid, Set<UUID> teamUUID, Set<String> caseTypes);

    @Query(value = "SELECT * FROM active_stage_data", nativeQuery = true)
    Set<Stage> findAllActive();

    @Query(value = "SELECT sd.* FROM stage sd JOIN case_data cd ON sd.case_uuid = cd.uuid WHERE sd.user_uuid = ?1 AND sd.team_uuid = ?2 AND NOT cd.deleted", nativeQuery = true)
    Set<BasicStage> findStageCaseUUIDsByUserUUIDTeamUUID(UUID userUUID, UUID teamUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd where sd.case_reference = ?1", nativeQuery = true)
    Set<Stage> findByCaseReference(String reference);

}
