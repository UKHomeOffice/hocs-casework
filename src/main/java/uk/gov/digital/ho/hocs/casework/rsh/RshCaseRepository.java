package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RshCaseRepository extends CrudRepository<RshCaseDetails, String> {

    @Query(value = "SELECT nextval('ref_seq')" , nativeQuery =
            true)
    Long getNextSeriesId();

    RshCaseDetails findByUuid(UUID uuid);
}