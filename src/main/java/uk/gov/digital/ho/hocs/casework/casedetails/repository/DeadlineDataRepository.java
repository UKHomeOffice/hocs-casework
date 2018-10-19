package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageDeadline;

import java.util.UUID;

@Repository
public interface DeadlineDataRepository extends CrudRepository<StageDeadline, String> {

    StageDeadline findByCaseUUIDAndStage(UUID caseUUID, String stage);

}
