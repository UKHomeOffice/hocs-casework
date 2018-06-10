package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseDetailsRepository extends CrudRepository<CaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    CaseDetails findByUuid(UUID uuid);

    @Query(value = "SELECT * from case_data cd where cd.reference = ?1 or cd.uuid in (SELECT rc.case_uuid from stage_data rc where rc.data->>'legacy-reference' = ?1)", nativeQuery = true)
    Set<CaseDetails> findByCaseReference(String caseReference);

    @Query(value = "SELECT * from case_data cd where cd.uuid in (SELECT rc.case_uuid from stage_data rc where LOWER(CAST(rc.data->>'first-name' as text)) = LOWER(?1) or LOWER(CAST(rc.data->>'last-name' as text)) = LOWER(?2) or rc.data->>'date-of-birth' = ?3)", nativeQuery = true)
    Set<CaseDetails> findByNameOrDob(String firstName, String lastName, String dob);

}
