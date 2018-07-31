package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStageData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ActiveStageDataRepository extends CrudRepository<ActiveStageData, String> {

    void deleteAllByStageUUID(UUID stageUUID);

    Set<ActiveStageData> findAllByStageUUID(UUID stageUUID);
}