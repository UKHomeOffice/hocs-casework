package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseNoteRepository extends CrudRepository<CaseNote, String> {

    @Query(value = "SELECT acn.* FROM active_case_note acn JOIN active_case aca ON acn.case_uuid = aca.uuid WHERE acn.uuid = ?1", nativeQuery = true)
    Set<CaseNote> findAllByCaseUUID(UUID caseUUID);
}