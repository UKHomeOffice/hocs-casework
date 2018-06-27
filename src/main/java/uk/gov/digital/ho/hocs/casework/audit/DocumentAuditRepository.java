package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.DocumentAudit;

@Repository
interface DocumentAuditRepository extends CrudRepository<DocumentAudit, String> {

}