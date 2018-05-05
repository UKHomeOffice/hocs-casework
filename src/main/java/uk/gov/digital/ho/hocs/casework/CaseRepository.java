package uk.gov.digital.ho.hocs.casework;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends CrudRepository<CaseDetails, String> {

    @Query(value = "SELECT nextval('ref_seq')" , nativeQuery =
            true)
    Long getNextSeriesId();
}