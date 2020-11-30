package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.Set;
import java.util.UUID;

@Repository
public interface SomuItemRepository extends CrudRepository<SomuItem, Long> {

    SomuItem findByUuid(UUID uuid);

    Set<SomuItem> findAllByCaseUuid(UUID caseUuid);

    SomuItem findByCaseUuidAndSomuUuid(UUID caseUuid, UUID somuUuid);

}
