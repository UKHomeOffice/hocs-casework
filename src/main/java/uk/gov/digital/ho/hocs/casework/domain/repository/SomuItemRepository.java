package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SomuItemRepository extends CrudRepository<SomuItem, Long> {

    @Query(value = "SELECT s.* FROM somu_item s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.uuid = ?1 AND NOT cd.deleted",
           nativeQuery = true)
    SomuItem findByUuid(UUID uuid);

    @Query(value = "SELECT s.* FROM somu_item s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid = ?1 AND NOT cd.deleted",
           nativeQuery = true)
    Set<SomuItem> findAllByCaseUuid(UUID caseUuid);

    @Query(value = "SELECT s.* FROM somu_item s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid IN ?1 AND NOT cd.deleted",
           nativeQuery = true)
    Collection<SomuItem> findAllByCaseUuidIn(Collection<UUID> caseUuids);

    @Query(value = "SELECT s.* FROM somu_item s JOIN case_data cd ON s.case_uuid = cd.uuid WHERE s.case_uuid = ?1 AND s.somu_uuid = ?2 AND NOT cd.deleted",
           nativeQuery = true)
    Set<SomuItem> findByCaseUuidAndSomuUuid(UUID caseUuid, UUID somuUuid);

}
