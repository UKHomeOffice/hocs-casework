package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.springframework.data.repository.CrudRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.Document;

import java.util.UUID;

public interface DocumentRepository extends CrudRepository<Document, String> {

    Document findByDocumentUuid(UUID uuid);
}
