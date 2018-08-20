package uk.gov.digital.ho.hocs.casework.audit.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;

@Repository
public interface AuditRepository extends CrudRepository<AuditEntry, Long> {

}