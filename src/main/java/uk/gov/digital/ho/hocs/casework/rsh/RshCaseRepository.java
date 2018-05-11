package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.dto.SearchResponse;

import java.util.Set;

@Repository
public interface RshCaseRepository extends CrudRepository<RshCaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    RshCaseDetails findByUuid(String uuid);


    @Query(value = "SELECT rc.data->>'case-ref' as caseReference, rc.uuid, rc.data->>'first-name' as firstName, rc.data->>'last-name' as lastName, rc.data->>'date-of-birth' as datOfBirth, rc.data->>'outcome' as outcome from rsh_case rc where rc.reference = ?1 or rc.data->>'legacy-reference' = ?1", nativeQuery = true)
    SearchResponse findByCaseReference(String caseReference);

    @Query(value = "SELECT rc.data->>'case-ref' as caseReference, rc.uuid, rc.data->>'first-name' as firstName, rc.data->>'last-name' as lastName, rc.data->>'date-of-birth' as dateOfBirth, rc.data->>'outcome' as outcome from rsh_case rc where (rc.data @> ?1 and rc.data @> ?2) or rc.data @> ?3", nativeQuery = true)
    Set<SearchResponse> findByNameOrDob(String firstName, String lastName, String dob);
}