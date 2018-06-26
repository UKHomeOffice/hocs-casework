package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.DocumentAudit;

import java.time.LocalDateTime;
import java.util.Set;

@Repository
interface DocumentAuditRepository extends CrudRepository<DocumentAudit, String> {

    Set<DocumentAudit> getAllByTimestampBetweenAndCorrespondenceTypeIn(LocalDateTime start, LocalDateTime end, String[] correspondenceTypes);
}