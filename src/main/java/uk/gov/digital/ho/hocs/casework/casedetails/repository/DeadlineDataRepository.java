package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DeadlineData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface DeadlineDataRepository extends CrudRepository<DeadlineData, String> {

    DeadlineData findByCaseUUIDAndStage(UUID caseUUID, String stage);

    Set<DeadlineData> findAllByCaseUUID(UUID caseUUID);
}
