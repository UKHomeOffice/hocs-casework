package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.repository.AuditRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

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


    public void createCaseEvent(CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, AuditAction.CREATE_CASE);
        save(auditEntry);
    }

    public void updateCaseEvent(CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, AuditAction.UPDATE_CASE);
        save(auditEntry);
    }

    public void getCaseEvent(UUID caseUUID) {
        String caseString = null;
        if (caseUUID != null) {
            caseString = caseUUID.toString();
        }
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseString, AuditAction.GET_CASE);
        save(auditEntry);
    }

    public void createStageEvent(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        //AuditEntry auditEntry = new AuditEntry(requestData.username(), stageData, AuditAction.CREATE_STAGE);
        //save(auditEntry);
    }

    public void allocateStageEvent(UUID stageUUID, UUID teamUUID, UUID userUUID) {
    }

    public void setStageInactiveEvent(UUID stageUUID) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), stageUUID.toString(), AuditAction.SET_INACTIVE_STAGE);
        save(auditEntry);
    }

    public void setStageActiveEvent(UUID stageUUID) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), stageUUID.toString(), AuditAction.SET_ACTIVE_STAGE);
        save(auditEntry);
    }

    public void createInputDataEvent(InputData inputData) {
        //AuditEntry auditEntry = new AuditEntry(requestData.username(), stageData, AuditAction.UPDATE_STAGE);
        //save(auditEntry);
    }

    public void updateInputDataEvent(InputData inputData) {
        //AuditEntry auditEntry = new AuditEntry(requestData.username(), stageData, AuditAction.UPDATE_STAGE);
        //save(auditEntry);
    }

    public void createDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.ADD_DOCUMENT);
        save(auditEntry);
    }

    public void updateDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.UPDATE_DOCUMENT);
        save(auditEntry);
    }

    public void extractReportEvent(String params) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), params, AuditAction.CSV_EXTRACT);
        save(auditEntry);
    }

    public void getStageEvent(UUID stageUUID) {
    }

    private void save(AuditEntry auditEntry) {

        // auditRepository.save(auditEntry);
    }

}
