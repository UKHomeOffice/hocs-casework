package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

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

    @Query(value = "SELECT * FROM active_stage_data sd WHERE sd.team_uuid IN ?1", nativeQuery = true)
    Set<Stage> findAllActiveByTeamUUIDIn(Set<UUID> teamUUID);

}