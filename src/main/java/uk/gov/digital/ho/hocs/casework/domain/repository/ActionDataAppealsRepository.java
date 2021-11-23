package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActionDataAppealsRepository extends CrudRepository<ActionDataAppeal, Long> {

    List<ActionDataAppeal> findAllByCaseDataUuid(UUID caseDataUuid);

    List<ActionDataAppeal> findAllByCaseDataType(String caseDataType);

    ActionDataAppeal findByUuidAndCaseDataUuid(UUID uuid, UUID caseUuid);

    List<ActionDataAppeal> findAllByCaseTypeActionUuidAndCaseDataUuid(UUID caseTypeUuid, UUID caseUUID);
}
