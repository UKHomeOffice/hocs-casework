package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataExternalInterest;

import java.util.List;
import java.util.UUID;

public interface ActionDataExternalInterestRepository extends CrudRepository <ActionDataExternalInterest, UUID> {

    List<ActionDataExternalInterest> findAllByCaseDataUuid(UUID caseDataUuid);

    List<ActionDataExternalInterest> findAllByCaseDataType(String caseDataType);

    ActionDataExternalInterest findByCaseDataUuid(UUID actionDataUuid);

    ActionDataExternalInterest findByUuidAndCaseDataUuid(UUID uuid, UUID caseUuid);

    List<ActionDataExternalInterest> findAllByCaseTypeActionUuid(UUID caseTypeActionUuid);
}
