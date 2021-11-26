package uk.gov.digital.ho.hocs.casework.migration;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.UUID;

@Repository
public interface MigrationStageRepository extends CrudRepository<StageWithCaseData, Long> {

    @Query(value = "SELECT sd.* FROM active_stage_data sd WHERE sd.case_uuid = ?", nativeQuery = true)
    StageWithCaseData findByCaseUUID(UUID caseUUID);

}
