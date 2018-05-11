package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RshCaseRepository extends CrudRepository<RshCaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    RshCaseDetails findByUuid(String uuid);

    @Query(value = "SELECT * from rsh_case rc where rc.reference = ?1 or rc.data->>'legacy-reference' = ?1", nativeQuery = true)
    RshCaseDetails findByCaseReference(String caseReference);

    @Query(value = "SELECT * from rsh_case rc where (rc.data->>'first-name' = ?1 and rc.data->>'last-name' = ?2) or rc.data->>'date-of-birth' = ?3", nativeQuery = true)
    Set<RshCaseDetails> findByNameOrDob(String firstName, String lastName, String dob);
}