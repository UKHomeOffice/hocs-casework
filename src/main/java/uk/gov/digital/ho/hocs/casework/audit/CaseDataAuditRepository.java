package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.CaseDataAudit;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
interface CaseDataAuditRepository extends CrudRepository<CaseDataAudit, String> {

    @Query(value = "select cd.* from audit_case_data cd join (select max(c.id) as id, c.uuid as uuid from audit_case_data c where c.type in ?3 and c.created between ?1 and ?2 group by c.uuid ) scd on cd.id = scd.id order by cd.created desc", nativeQuery = true)
    Set<CaseDataAudit> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, String[] correspondenceTypes);
}