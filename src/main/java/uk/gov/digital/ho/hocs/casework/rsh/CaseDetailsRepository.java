package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseDetailsRepository extends CrudRepository<CaseDetails, String> {

    @Query(value = "SELECT nextval('case_ref')" , nativeQuery = true)
    Long getNextSeriesId();

    CaseDetails findByUuid(UUID uuid);

    @Query(value = "SELECT * from case_details rc where rc.reference = ?1 or rc.data->>'legacy-reference' = ?1", nativeQuery = true)
    CaseDetails findByCaseReference(String caseReference);
    }