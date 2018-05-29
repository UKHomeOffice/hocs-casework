package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseDetailsRepository extends CrudRepository<CaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    CaseDetails findByUuid(UUID uuid);

    @Query(value = "SELECT * from case_details cd where cd.reference = ?1 or cd.uuid in (SELECT rc.case_uuid from stage_details rc where rc.data->>'legacy-reference' = ?1)", nativeQuery = true)
    Set<CaseDetails> findByCaseReference(String caseReference);

    @Query(value = "SELECT * from case_details cd where cd.uuid in (SELECT rc.case_uuid from stage_details rc where (LOWER(CAST(rc.data->>'first-name' as text)) = LOWER(?1) or LOWER(CAST(rc.data->>'last-name' as text)) = LOWER(?2)) and rc.data->>'date-of-birth' = ?3)", nativeQuery = true)
    Set<CaseDetails> findByNameOrDob(String firstName, String lastName, String dob);

    //@Query(value = "SELECT cd.* from case_details cd, (select max(id) as id, max(created), c.case_uuid from case_details c where c.correspondence_type in ?3 and c.msg_timestamp between ?1 and ?2 GROUP BY c.case_reference) scd where cd.id = scd.id", nativeQuery = true)
    //Set<CaseDetails> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, String[] correspondenceTypes);


}