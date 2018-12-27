package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CaseNoteRepository extends CrudRepository<CaseNote, Long> {

    @Query(value = "SELECT acn.* FROM active_case_note acn WHERE acn.case_uuid = ?1", nativeQuery = true)
    Set<CaseNote> findAllByCaseUUID(UUID caseUUID);

    @Query(value = "SELECT acn.* FROM active_case_note acn WHERE acn.case_uuid = ?1 AND acn.stage_type IN ?2", nativeQuery = true)
    Set<CaseNote> findLatestByStageTypeForStageUUID(UUID stageUUID, Set<String> stageTypes);

    CaseNote findByUuid(UUID caseNoteUUID);
}