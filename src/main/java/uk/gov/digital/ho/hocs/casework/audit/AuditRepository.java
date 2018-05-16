package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditRepository extends CrudRepository<AuditEntry, Long> {

    AuditEntry findByUUID(String uuid);
}