package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<DocumentData, String> {

    DocumentData findByUuid(UUID uuid);

    Set<DocumentData> findAllByCaseUUID(UUID caseUuid);
}