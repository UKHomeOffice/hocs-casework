package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Repository
public interface AuditCaseDetailsRepository extends CrudRepository<AuditCaseData, String> {

    @Query(value = "select cd.* from casework.audit_case_data cd join (select max(c.id) as id, c.uuid as uuid from casework.audit_case_data c where c.type in ?3 and c.created between ?1 and ?2 group by c.uuid ) scd on cd.id = scd.id order by cd.created desc", nativeQuery = true)
    Set<AuditCaseData> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, String[] correspondenceTypes);
}