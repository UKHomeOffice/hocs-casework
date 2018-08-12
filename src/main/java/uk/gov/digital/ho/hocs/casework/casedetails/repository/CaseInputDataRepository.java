package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;

import java.util.UUID;

@Repository
public interface CaseInputDataRepository extends CrudRepository<CaseInputData, String> {

    CaseInputData findByCaseUUID(UUID uuid);

}