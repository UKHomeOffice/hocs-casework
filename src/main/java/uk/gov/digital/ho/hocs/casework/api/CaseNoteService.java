package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class CaseNoteService {

    private final CaseNoteRepository caseNoteRepository;

    @Autowired
    public CaseNoteService(CaseNoteRepository caseNoteRepository) {
        this.caseNoteRepository = caseNoteRepository;
    }

    Set<CaseNote> getCaseNotes(UUID caseUUID) {
        log.debug("Getting all CaseNotes for Case: {}", caseUUID);
        Set<CaseNote> caseNotes = caseNoteRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} CaseNotes for Case: {}", caseNotes.size(), caseUUID, value(EVENT, CASE_NOTE_RETRIEVED));
        return caseNotes;
    }

    void createCaseNote(UUID caseUUID, String caseNoteType, String text) {
        log.debug("Creating CaseNote of Type: {} for Case: {}", caseNoteType, caseUUID);
        CaseNote caseNote = new CaseNote(caseUUID, caseNoteType, text);
        caseNoteRepository.save(caseNote);
        log.info("Created CaseNote: {} for Case: {}", caseNote.getUuid(), caseUUID, value(EVENT, CASE_NOTE_CREATED));
    }
}