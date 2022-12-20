package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;

import java.util.List;
import java.util.UUID;

public interface ActionDataDeadlineExtensionRepository extends CrudRepository<ActionDataDeadlineExtension, UUID> {

    List<ActionDataDeadlineExtension> findAllByCaseDataUuid(UUID caseDataUuid);

    List<ActionDataDeadlineExtension> findAllByCaseTypeActionUuidAndCaseDataUuid(UUID caseTypeActionUuid,
                                                                                 UUID caseUUID);
    boolean existsDistinctByCaseDataUuid(UUID caseDataUuid);

}
