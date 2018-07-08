package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.UUID;

public interface DocumentRepository extends CrudRepository<DocumentData, String> {

    DocumentData findByUuid(UUID uuid);

}
