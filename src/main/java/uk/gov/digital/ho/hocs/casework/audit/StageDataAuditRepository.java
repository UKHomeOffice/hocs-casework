package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.StageDataAudit;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Repository
interface StageDataAuditRepository extends CrudRepository<StageDataAudit, String> {

    @Query(value = "select osd.* from audit_stage_data osd join (select max(sd.id) as id, sd.uuid as uuid from audit_stage_data sd join (select max(c.id) as id, c.uuid as uuid from audit_case_data c where c.type in ?3 and c.timestamp between ?1 and ?2 group by c.uuid ) scd on sd.case_uuid = scd.uuid and sd.timestamp between ?1 and ?2 group by sd.uuid) isd on osd.id = isd.id order by osd.timestamp desc", nativeQuery = true)
    Set<StageDataAudit> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, List<String> correspondenceTypes);
}