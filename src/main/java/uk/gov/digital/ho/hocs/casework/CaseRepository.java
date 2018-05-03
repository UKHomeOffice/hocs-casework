package uk.gov.digital.ho.hocs.casework;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CaseRepository extends CrudRepository<CaseDetails, String> {

}