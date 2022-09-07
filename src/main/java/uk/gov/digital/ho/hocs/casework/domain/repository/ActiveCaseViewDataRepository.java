package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveCaseViewData;

import java.util.UUID;

@Repository
public interface ActiveCaseViewDataRepository extends CrudRepository<ActiveCaseViewData, Long> {

    @Query(value = "SELECT ac.* FROM active_case ac where ac.uuid = ?1", nativeQuery = true)
    ActiveCaseViewData findByUuid(UUID uuid);

}
