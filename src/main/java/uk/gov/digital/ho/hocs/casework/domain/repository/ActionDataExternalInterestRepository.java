package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataExternalInterest;

import java.util.List;
import java.util.UUID;

public interface ActionDataExternalInterestRepository extends CrudRepository<ActionDataExternalInterest, UUID> {

    List<ActionDataExternalInterest> findAllByCaseDataUuid(UUID caseDataUuid);

    ActionDataExternalInterest findByUuidAndCaseDataUuid(UUID uuid, UUID caseUuid);

}
