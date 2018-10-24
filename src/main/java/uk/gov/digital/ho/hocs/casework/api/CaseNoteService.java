package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class CaseNoteService {

    private final CaseNoteRepository caseNoteRepository;

    @Autowired
    public CaseNoteService(CaseNoteRepository caseNoteRepository) {
        this.caseNoteRepository = caseNoteRepository;
    }

    @Transactional
    public Set<CaseNote> getCaseNotes(UUID caseUUID) {
        Set<CaseNote> caseNotes = caseNoteRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} CaseNotes for Case: {}", caseNotes.size(), caseUUID);
        return caseNotes;
    }

    @Transactional
    public CaseNote createCaseNote(UUID caseUUID, CaseNoteType caseNoteType, String text) {
        CaseNote caseNote = new CaseNote(caseUUID, caseNoteType, text);
        caseNoteRepository.save(caseNote);
        log.info("Created CaseNote: {} for Case: {}", caseNote.getUuid(), caseUUID);
        return caseNote;
    }
}