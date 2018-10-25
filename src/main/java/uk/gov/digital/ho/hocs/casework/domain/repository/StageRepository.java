package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageRepository extends CrudRepository<Stage, String> {

    @Query(value = "SELECT * FROM stage_data sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findByUuid(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT * FROM stage_data sd WHERE sd.user_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByTeamUUID(UUID userUUID);

    @Query(value = "SELECT * FROM stage_data sd WHERE sd.team_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByUserUID(UUID teamUUID);

    @Modifying
    @Query(value = "UPDATE stage s SET team_uuid = ?3, user_uuid = ?4, status = ?5 WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    int update(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID, StageStatusType stageStatusType);

}