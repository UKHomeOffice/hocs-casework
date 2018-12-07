package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageRepository extends CrudRepository<Stage, Long> {

    @Query(value = "SELECT sd.* FROM active_stage sd WHERE sd.case_uuid = ?1 AND sd.uuid = ?2", nativeQuery = true)
    Stage findByUuid(UUID caseUUID, UUID stageUUID);

    @Query(value = "SELECT sd.* FROM active_stage sd WHERE sd.user_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByTeamUUID(UUID userUUID);

    @Query(value = "SELECT sd.* FROM active_stage sd WHERE sd.team_uuid = ?1", nativeQuery = true)
    Set<Stage> findAllByUserUUID(UUID teamUUID);

    @Query(value = "SELECT sd.* FROM active_stage sd WHERE sd.case_uuid = ?1", nativeQuery = true)
    Set<Stage> findActiveStagesByCaseUUID(UUID teamUUID);

    @Query(value = "SELECT * FROM active_stage sd WHERE sd.team_uuid IN ?1", nativeQuery = true)
    Set<Stage> findAllBy(Set<UUID> teamUUID);
}