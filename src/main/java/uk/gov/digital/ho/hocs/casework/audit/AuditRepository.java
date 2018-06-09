package uk.gov.digital.ho.hocs.casework.audit;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuditRepository extends CrudRepository<AuditEntry, Long> {

}