package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;

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
    @Query(value = "UPDATE stage s SET complete = TRUE WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    int complete(UUID caseUUID, UUID stageUUID);

    @Modifying
    @Query(value = "UPDATE stage s SET team_uuid = ?3, user_uuid = NULL WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    int allocateToTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID);

    @Modifying
    @Query(value = "UPDATE stage s SET team_uuid = ?3, user_uuid = ?4 WHERE s.case_uuid = ?1 AND s.uuid = ?2", nativeQuery = true)
    int allocateToUser(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID);

}