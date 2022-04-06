package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataSuspension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensionRepository extends CrudRepository<ActionDataSuspension, UUID> {

    Optional<List<ActionDataSuspension>> findAllByCaseDataUuid(UUID caseUUID);

    Optional<ActionDataSuspension> findByUuidAndCaseDataUuid(UUID uuid, UUID caseUUID);

    Optional<List<ActionDataSuspension>> findAllByCaseDataUuidAndDateSuspensionRemovedIsNull(UUID caseUUID);
}
