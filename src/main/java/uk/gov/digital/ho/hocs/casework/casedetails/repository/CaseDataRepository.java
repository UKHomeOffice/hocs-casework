package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseDataRepository extends CrudRepository<CaseData, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    CaseData findByUuid(UUID uuid);

    @Query(value = "SELECT * from case_data cd where cd.reference = ?1 or cd.uuid in (SELECT rc.case_uuid from stage_data rc where LOWER(CAST(rc.data->>'legacy-reference' as text)) = LOWER(?1))", nativeQuery = true)
    Set<CaseData> findByCaseReference(String caseReference);

    @Query(value = "SELECT * from case_data cd where cd.uuid in (SELECT rc.case_uuid from stage_data rc where LOWER(CAST(rc.data->>'first-name' as text)) = LOWER(?1) or LOWER(CAST(rc.data->>'last-name' as text)) = LOWER(?2) or rc.data->>'date-of-birth' = ?3)", nativeQuery = true)
    Set<CaseData> findByNameOrDob(String firstName, String lastName, String dob);
}