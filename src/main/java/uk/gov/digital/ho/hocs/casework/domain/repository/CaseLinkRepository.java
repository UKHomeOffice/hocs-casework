package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLinkId;

@Repository
public interface CaseLinkRepository extends CrudRepository<CaseLink, CaseLinkId> {

}
