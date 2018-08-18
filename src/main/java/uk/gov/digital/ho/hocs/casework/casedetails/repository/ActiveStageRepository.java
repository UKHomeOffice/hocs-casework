package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import java.util.Set;

@Repository
public interface ActiveStageRepository extends CrudRepository<ActiveStage, String> {

    @Query(value = "SELECT cd.reference as case_reference, cd.type as case_type, sd.* from stage_data sd JOIN case_data cd on sd.case_uuid = cd.uuid WHERE sd.active = TRUE", nativeQuery = true)
    Set<ActiveStage> findAllActiveStages();
}