package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;

import java.util.UUID;

@Repository
public interface InputDataRepository extends CrudRepository<InputData, String> {

    InputData findByCaseUUID(UUID uuid);

}