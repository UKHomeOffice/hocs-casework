package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.UUID;

@Repository
public interface CaseDataRepository extends CrudRepository<CaseData, Long> {

    @Query(value = "SELECT nextval('case_ref')", nativeQuery = true)
    Long getNextSeriesId();

    @Query(value = "SELECT ac.* FROM active_case ac where ac.uuid = ?1", nativeQuery = true)
    CaseData findByUuid(UUID uuid);

    @Query(value = "SELECT cd.reference FROM case_data cd where cd.uuid = ?1", nativeQuery = true)
    String getCaseRef(UUID uuid);

}