package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditStageData;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
interface AuditStageDetailsRepository extends CrudRepository<AuditStageData, String> {

    @Query(value = "select osd.* from audit_stage_data osd join (select max(sd.id) as id, sd.uuid as uuid from audit_stage_data sd join (select max(c.id) as id, c.uuid as uuid from audit_case_data c where c.type in ?3 and c.created between ?1 and ?2 group by c.uuid ) scd on sd.case_uuid = scd.uuid and sd.created between ?1 and ?2 group by sd.uuid) isd on osd.id = isd.id order by osd.created desc", nativeQuery = true)
    Set<AuditStageData> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, String[] correspondenceTypes);
}