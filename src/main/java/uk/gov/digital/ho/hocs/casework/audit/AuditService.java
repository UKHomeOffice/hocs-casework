package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.rsh.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.UUID;

@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;

    @Autowired
    public AuditService(AuditRepository auditRepository, RequestData requestData, ObjectMapper objectMapper) {
        this.auditRepository = auditRepository;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    public void writeSearchEvent(SearchRequest searchRequest) {
        String request = SearchRequest.toJsonString(objectMapper, searchRequest);
        AuditEntry auditEntry = new AuditEntry(requestData.username(), request, AuditAction.SEARCH);
        save(auditEntry);
    }

    public void writeSendEmailEvent(SendEmailRequest sendEmailRequest) {
        String emailString = null;
        if (sendEmailRequest != null) {
            emailString = sendEmailRequest.getEmailAddress();
        }
        AuditEntry auditEntry = new AuditEntry(requestData.username(), emailString, AuditAction.SEND_EMAIL);
        save(auditEntry);
    }

    public void writeGetCaseEvent(UUID caseUUID) {
        String caseString = null;
        if (caseUUID != null) {
            caseString = caseUUID.toString();
        }
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseString, AuditAction.GET_CASE);
        save(auditEntry);
    }

    public void writeCreateCaseEvent(CaseData caseData, CaseInputData caseInputData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, AuditAction.CREATE_CASE);
        save(auditEntry);
    }

    public void writeUpdateCaseEvent(CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, AuditAction.UPDATE_CASE);
        save(auditEntry);
    }

    public void writeCreateStageEvent(StageData stageData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), stageData, AuditAction.CREATE_STAGE);
        save(auditEntry);
    }

    public void writeUpdateStageEvent(UUID stageUUID, CaseInputData caseInputData) {
        //AuditEntry auditEntry = new AuditEntry(requestData.username(), stageData, AuditAction.UPDATE_STAGE);
        //save(auditEntry);
    }

    public void writeCompleteStageEvent(UUID stageUUID) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), stageUUID.toString(), AuditAction.COMPLETE_STAGE);
        save(auditEntry);
    }

    public void writeCreateDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.ADD_DOCUMENT);
        save(auditEntry);
    }

    public void writeUpdateDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.UPDATE_DOCUMENT);
        save(auditEntry);
    }

    public void writeExtractEvent(String params) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), params, AuditAction.CSV_EXTRACT);
        save(auditEntry);
    }

    private void save(AuditEntry auditEntry) {
        //auditRepository.save(auditEntry);
    }

    public void writeAllocateStageEvent(StageData stageData) {
    }

    public void writeGetStageEvent(UUID stageUUID) {
    }
}
