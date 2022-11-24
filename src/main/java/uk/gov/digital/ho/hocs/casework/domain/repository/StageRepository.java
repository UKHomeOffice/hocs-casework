package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface StageRepository extends CrudRepository<BaseStage, Long> {

    @Query(value = "SELECT s.* FROM stage s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid = ?1 AND s.uuid = ?2 AND s.team_uuid IS NOT NULL AND NOT cd.deleted",
           nativeQuery = true)
    Stage findActiveBasicStageByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    Optional<Stage> findFirstByTeamUUIDIsNotNullAndCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByCaseUUIDAsStage(UUID caseUUID);

    @Query(value = "SELECT s.* FROM stage s WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    Stage findBasicStageByCaseUuidAndStageUuid(UUID caseUuid, UUID stageUuid);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2 AND sd.team_uuid IS NOT NULL",
           nativeQuery = true)
    StageWithCaseData findActiveByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    StageWithCaseData findByCaseUuidStageUUID(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.team_uuid IS NOT NULL",
           nativeQuery = true)
    Set<StageWithCaseData> findAllActiveByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<StageWithCaseData> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.case_uuid IN ?1", nativeQuery = true)
    Set<StageWithCaseData> findAllByCaseUUIDIn(Set<UUID> caseUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.team_uuid = ?1", nativeQuery = true)
    Set<StageWithCaseData> findAllActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.team_uuid in ?1", nativeQuery = true)
    Set<StageWithCaseData> findAllActiveByTeamUUID(Set<UUID> teamUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd WHERE sd.team_uuid = ?1 AND sd.user_uuid IS NULL",
           nativeQuery = true)
    Set<StageWithCaseData> findAllUnassignedAndActiveByTeamUUID(UUID teamUUID);

    @Query(value = "SELECT sd.* FROM stage sd JOIN case_data cd ON sd.case_uuid = cd.uuid WHERE sd.user_uuid = ?1 AND sd.team_uuid = ?2 AND NOT cd.deleted",
           nativeQuery = true)
    Set<Stage> findStageCaseUUIDsByUserUUIDTeamUUID(UUID userUUID, UUID teamUUID);

    @Query(value = "SELECT sd.* FROM stage_data sd where sd.case_reference = ?1", nativeQuery = true)
    Set<StageWithCaseData> findByCaseReference(String reference);

    @Query(value = "SELECT s.* FROM stage s JOIN stage_data sd ON s.type = sd.type WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllStagesByCaseUuid(UUID casUuid);

}
