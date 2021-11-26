package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseNoteRepository extends CrudRepository<CaseNote, Long> {

    @Query(value = "SELECT cn.* FROM case_note cn JOIN case_data cd on cn.case_uuid = cd.uuid WHERE cn.case_uuid = ?1 AND NOT cn.deleted AND NOT cd.deleted", nativeQuery = true)
    Set<CaseNote> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT cn.* FROM case_note cn JOIN case_data cd on cn.case_uuid = cd.uuid WHERE cn.uuid = ?1 AND NOT cn.deleted AND NOT cd.deleted", nativeQuery = true)
    CaseNote findByUuid(UUID caseNoteUUID);
}
