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

    // <0.2ms
    @Query(value = "SELECT ac.* FROM case_data ac where ac.uuid = ?1 AND ac.deleted = false", nativeQuery = true)
    CaseData findActiveByUuid(UUID uuid);

    // <0.2ms
    @Query(value = "SELECT ac.* FROM case_data ac where ac.reference = ?1", nativeQuery = true)
    CaseData findByReference(String reference);

    // <0.2ms
    @Query(value = "SELECT ac.reference FROM case_data ac where ac.uuid = ?1", nativeQuery = true)
    String getCaseRef(UUID uuid);

    // <0.2ms
    @Query(value = "SELECT ac.type FROM case_data ac where ac.uuid = ?1", nativeQuery = true)
    String getCaseType(UUID uuid);

    // <1ms
    @Query(value = "SELECT cd.* FROM view_case_data cd WHERE cd.uuid = ?1", nativeQuery = true)
    CaseData findAnyByUuid(UUID uuid);

    // <0.2ms
    @Query(value = "SELECT ac.reference FROM case_data ac where ac.uuid = ?1", nativeQuery = true)
    String getCaseDataCaseRef(UUID uuid);
}
