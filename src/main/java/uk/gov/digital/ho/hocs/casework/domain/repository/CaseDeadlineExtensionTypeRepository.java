package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDeadlineExtensionType;

@Repository
public interface CaseDeadlineExtensionTypeRepository extends CrudRepository<CaseDeadlineExtensionType, String> {

}