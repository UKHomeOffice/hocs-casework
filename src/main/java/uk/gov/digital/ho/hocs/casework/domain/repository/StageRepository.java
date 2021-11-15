package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface StageRepository extends CrudRepository<Stage, Long> {

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findActiveByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllActiveByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid IN ?1", nativeQuery = true)
    Set<Stage> findAllByCaseUUIDIn(Set<UUID> caseUUID);

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.team_uuid = ?1", nativeQuery = true)
    List<Stage> findAllActiveByTeamUUID(UUID teamUUID);

    @Query(
            value = "SELECT sd.* FROM active_stage_data sd WHERE sd.team_uuid = ?1 AND sd.user_uuid IS NULL",
            nativeQuery = true
    )
    Set<Stage> findAllUnassignedAndActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT * FROM active_stage_data sd WHERE sd.team_uuid IN ?1 OR sd.case_type IN ?2", nativeQuery = true)
    List<Stage> findAllActiveByTeamUUIDAndCaseType(Set<UUID> teamUUID, Set<String> caseTypes);

    @Query(value = "SELECT * FROM active_stage_data sd WHERE user_uuid = ?1 AND NOT data @> CAST('{\"Unworkable\":\"True\"}' AS JSONB) AND (sd.team_uuid IN ?2 OR sd.case_type IN ?3)", nativeQuery = true)
    List<Stage> findAllActiveByUserUuidAndTeamUuidAndCaseType(UUID userUuid, Set<UUID> teamUUID, Set<String> caseTypes);

    @Query(value = "SELECT * FROM active_stage_data", nativeQuery = true)
    Set<Stage> findAllActive();

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.user_uuid = ?1 AND sd.team_uuid = ?2", nativeQuery = true)
    Set<Stage> findStageCaseUUIDsByUserUUIDTeamUUID(UUID userUUID, UUID teamUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd join active_case ac on sd.case_uuid = ac.uuid where ac.reference = ?1", nativeQuery = true)
    Set<Stage> findByCaseReference(String reference);

    @SuppressWarnings("SpringDataRepositoryMethodReturnTypeInspection") // caused by using 'By' in method name
    @Query(value = "SELECT CAST(team_uuid AS VARCHAR(36)) AS teamUuid FROM stage WHERE case_uuid = ?1 AND uuid = ?2", nativeQuery = true)
    Stage.StageTeamUuid findTeamUuidByCaseUuidAndStageUuid(UUID caseUuid, UUID stageUuid);

}
