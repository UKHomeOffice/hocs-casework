package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Reference;

import java.util.Set;
import java.util.UUID;

@Repository
public interface ReferenceDataRepository extends CrudRepository<Reference, String> {

    @Query(value = "SELECT * FROM reference_data WHERE case_uuid =?1", nativeQuery = true)
    Set<Reference> findByCaseUUID(UUID caseUUID);



}
