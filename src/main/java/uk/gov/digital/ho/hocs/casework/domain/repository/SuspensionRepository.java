package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataSuspension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SuspensionRepository extends CrudRepository<ActionDataSuspension, UUID> {

    List<ActionDataSuspension> findAllByCaseDataUuid(UUID caseUUID);

    Optional<ActionDataSuspension> findByUuidAndCaseDataUuid(UUID uuid, UUID caseUUID);

    List<ActionDataSuspension> findAllByCaseDataUuidAndCaseTypeActionUuidAndDateSuspensionRemovedIsNull(UUID caseUUID,
                                                                                                        UUID caseActionTypeUUID);

}
