package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteData;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseNoteDataRepository extends CrudRepository<CaseNoteData, String> {

    Set<CaseNoteData> findAllByCaseUUID(UUID uuid);

}