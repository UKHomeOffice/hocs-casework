package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseNoteRepository;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_DELETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_NOTE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class CaseNoteService {

    private static final String NOTE_NOT_FOUND_MESSAGE = "CaseNote for UUID: %s not found!";

    private final CaseNoteRepository caseNoteRepository;

    private final AuditClient auditClient;

    private final RequestData requestData;

    @Autowired
    public CaseNoteService(CaseNoteRepository caseNoteRepository, AuditClient auditClient, RequestData requestData) {
        this.caseNoteRepository = caseNoteRepository;
        this.auditClient = auditClient;
        this.requestData = requestData;
    }

    Set<CaseNote> getCaseNotes(UUID caseUUID) {
        log.debug("Getting all CaseNotes for Case: {}", caseUUID);
        Set<CaseNote> caseNotes = caseNoteRepository.findAllByCaseUUID(caseUUID);
        log.info("Got {} CaseNotes for Case: {}", caseNotes.size(), caseUUID, value(EVENT, CASE_NOTE_RETRIEVED));
        auditClient.viewCaseNotesAudit(caseUUID);
        return caseNotes;
    }

    public CaseNote getCaseNote(UUID caseNoteUUID) {
        CaseNote caseNote = caseNoteRepository.findByUuid(caseNoteUUID);
        if (caseNote != null) {
            log.info("Got CaseNote for UUID: {}", caseNoteUUID, value(EVENT, CASE_NOTE_RETRIEVED));
            auditClient.viewCaseNoteAudit(caseNote);
            return caseNote;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format(NOTE_NOT_FOUND_MESSAGE, caseNoteUUID),
                CASE_NOTE_NOT_FOUND);
        }
    }

    public CaseNote createCaseNote(UUID caseUUID, String caseNoteType, String text) {
        log.debug("Creating CaseNote of Type: {} for Case: {}", caseNoteType, caseUUID);
        CaseNote caseNote = new CaseNote(caseUUID, caseNoteType, text, requestData.userId());
        caseNoteRepository.save(caseNote);
        log.info("Created CaseNote: {} for Case: {}", caseNote.getUuid(), caseUUID, value(EVENT, CASE_NOTE_CREATED));
        auditClient.createCaseNoteAudit(caseNote);
        return caseNote;
    }

    public CaseNote updateCaseNote(UUID caseNoteUUID, String caseNoteType, String text) {
        log.debug("Updating CaseNote: {}", caseNoteUUID);
        CaseNote caseNote = caseNoteRepository.findByUuid(caseNoteUUID);
        if (caseNote != null) {
            String prevCaseNoteType = caseNote.getCaseNoteType();
            String prevText = caseNote.getText();
            caseNote.setCaseNoteType(caseNoteType);
            caseNote.setText(text);
            caseNote.setEdited(LocalDateTime.now());
            caseNote.setEditor(requestData.userId());
            caseNoteRepository.save(caseNote);
            log.info("Updated CaseNote: {} for Case: {}", caseNote.getUuid(), caseNote.getCaseUUID(),
                value(EVENT, CASE_NOTE_UPDATED));
            auditClient.updateCaseNoteAudit(caseNote, prevCaseNoteType, prevText);
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format(NOTE_NOT_FOUND_MESSAGE, caseNoteUUID),
                CASE_NOTE_NOT_FOUND);
        }
        return caseNote;
    }

    public CaseNote deleteCaseNote(UUID caseNoteUUID) {
        log.debug("Deleting CaseNote: {}", caseNoteUUID);
        CaseNote caseNote = caseNoteRepository.findByUuid(caseNoteUUID);
        if (caseNote != null) {
            caseNote.setDeleted(true);
            caseNoteRepository.save(caseNote);
            log.info("Deleted CaseNote: {} for Case: {}", caseNote.getUuid(), caseNote.getCaseUUID(),
                value(EVENT, CASE_NOTE_DELETED));
            auditClient.deleteCaseNoteAudit(caseNote);
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format(NOTE_NOT_FOUND_MESSAGE, caseNoteUUID),
                CASE_NOTE_NOT_FOUND);
        }
        return caseNote;
    }

}
