package uk.gov.digital.ho.hocs.casework.rsh;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.UUID;

@Repository
public interface StageDetailsRepository extends CrudRepository<StageDetails, String> {

    StageDetails findByUuid(UUID uuid);

    @Query(value = "SELECT * from case_details rc where (LOWER(CAST(rc.data->>'first-name' as text)) = LOWER(?1) and LOWER(CAST(rc.data->>'last-name' as text)) = LOWER(?2)) or rc.data->>'date-of-birth' = ?3", nativeQuery = true)
    Set<CaseDetails> findByNameOrDob(String firstName, String lastName, String dob);
}