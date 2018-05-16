package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RshCaseRepository extends CrudRepository<CaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    CaseDetails findByUuid(String uuid);

    @Query(value = "SELECT * from rsh_case rc where rc.reference = ?1 or rc.data->>'legacy-reference' = ?1", nativeQuery = true)
    CaseDetails findByCaseReference(String caseReference);

    @Query(value = "SELECT * from rsh_case rc where (LOWER(CAST(rc.data->>'first-name' as text)) = LOWER(?1) and LOWER(CAST(rc.data->>'last-name' as text)) = LOWER(?2)) or rc.data->>'date-of-birth' = ?3", nativeQuery = true)
    Set<CaseDetails> findByNameOrDob(String firstName, String lastName, String dob);
}