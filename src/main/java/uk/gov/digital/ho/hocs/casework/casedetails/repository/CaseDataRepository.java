package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.util.UUID;

@Repository
public interface CaseDataRepository extends CrudRepository<CaseData, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    @Query(value = "SELECT cd.* FROM case_data cd where cd.uuid = ?1 and cd.deleted = FALSE", nativeQuery = true)
    CaseData findByUuid(UUID uuid);

    @Modifying
    @Query(value = "UPDATE case_data cd SET cd.deleted = TRUE WHERE uuid = ?1", nativeQuery = true)
    int delete(UUID correspondentUUID);
}