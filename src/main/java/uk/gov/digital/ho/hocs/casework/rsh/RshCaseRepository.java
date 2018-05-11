package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RshCaseRepository extends CrudRepository<RshCaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    RshCaseDetails findByUuid(String uuid);

    RshCaseDetails findByCaseReference(String caseReference);

    @Query(value = "SELECT * from rsh_case rc where rc.data @> ?1 or rc.data @> ?2 or rc.data @> ?3", nativeQuery = true)
    Set<RshCaseDetails> findByNameOrDob(String firstName, String lastName, String dob);
}