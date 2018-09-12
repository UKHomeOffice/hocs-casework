package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CorrespondentDataRepository extends CrudRepository<CorrespondentData, String> {


    @Query(value = "SELECT c.*, cc.type AS type from correspondent_data c JOIN case_correspondent cc on cc.correspondent_uuid = c.uuid where cc.case_uuid = ?1", nativeQuery = true)
    Set<CorrespondentData> findByCaseUUID(UUID caseUUID);
}
