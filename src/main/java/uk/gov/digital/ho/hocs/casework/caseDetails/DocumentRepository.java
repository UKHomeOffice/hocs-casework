package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;

import java.util.UUID;

public interface DocumentRepository extends CrudRepository<DocumentData, String> {

    DocumentData findByDocumentUUID(UUID uuid);
}
