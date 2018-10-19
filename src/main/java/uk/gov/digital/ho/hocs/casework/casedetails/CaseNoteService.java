package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseNoteType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseNoteRepository;

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
    public CaseNote createCaseNote(UUID caseUUID, CaseNoteType caseNoteType, String caseNote) {
        CaseNote caseNoteData = new CaseNote(caseUUID, caseNoteType, caseNote);
        caseNoteRepository.save(caseNoteData);
        log.info("Created CaseNote for Case: {}", caseUUID);
        return caseNoteData;
    }

    public Set<CaseNote> getCaseNotesForCase(UUID caseUUID) {
        Set<CaseNote> caseNotes = caseNoteRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} CaseNotes for Case: {}", caseNotes.size(), caseUUID);
        return caseNotes;
    }
}