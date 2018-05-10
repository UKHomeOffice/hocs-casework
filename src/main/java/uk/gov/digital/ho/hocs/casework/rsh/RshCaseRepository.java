package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RshCaseRepository extends CrudRepository<RshCaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    RshCaseDetails findByUuid(String uuid);
}