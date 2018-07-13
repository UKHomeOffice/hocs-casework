package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ScreenData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ScreenDataRepository extends CrudRepository<ScreenData, String> {

    void deleteAllByStageUUID(UUID stageUUID);

    Set<ScreenData> findAllByStageUUID(UUID stageUUID);
}