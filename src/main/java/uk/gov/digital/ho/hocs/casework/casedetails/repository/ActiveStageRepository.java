package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ActiveStageRepository extends CrudRepository<ActiveStage, String> {

    ActiveStage findByStageUUID(UUID stageUUID);

    Set<ActiveStage> findAll();

    Set<ActiveStage> findAllByUserUUID(UUID userUUID);

    Set<ActiveStage> findAllByTeamUUIDIn(Set<UUID> teamUUIDs);

    void deleteByStageUUID(UUID stageUUID);
}