package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.DocumentAuditEntry;

@Repository
interface DocumentAuditRepository extends CrudRepository<DocumentAuditEntry, String> {

}