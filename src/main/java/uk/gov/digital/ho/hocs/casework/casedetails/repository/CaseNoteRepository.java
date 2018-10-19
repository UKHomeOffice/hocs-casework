package uk.gov.digital.ho.hocs.casework.casedetails.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNote;

import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseNoteRepository extends CrudRepository<CaseNote, String> {

    @Query(value = "SELECT cn.* FROM case_note cn JOIN case_data cd ON cn.case_uuid = cd.uuid WHERE cd.uuid = ?1 AND cd.deleted = FALSE AND cn.deleted = FALSE", nativeQuery = true)
    Set<CaseNote> findAllByCaseUUID(UUID uuid);

}