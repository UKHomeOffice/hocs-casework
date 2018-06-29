package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;

    @Autowired
    public AuditService(AuditRepository auditRepository, RequestData requestData) {
        this.auditRepository = auditRepository;
        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
        this.requestData = requestData;
    }

    public void writeSearchEvent(SearchRequest searchRequest) {
        String request = SearchRequest.toJsonString(objectMapper, searchRequest);
        AuditEntry auditEntry = new AuditEntry(requestData.username(), request, AuditAction.SEARCH);
        auditRepository.save(auditEntry);
    }

    public void writeSendEmailEvent(SendEmailRequest sendEmailRequest) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), sendEmailRequest.getEmailAddress(), AuditAction.SEND_EMAIL);
        auditRepository.save(auditEntry);
    }

    public void writeGetCaseEvent(UUID caseUUID) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseUUID.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeCreateCaseEvent(CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, null, AuditAction.CREATE_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeUpdateCaseEvent(CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), caseData, null, AuditAction.UPDATE_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeCreateStageEvent(StageData stageData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), null, stageData, AuditAction.CREATE_STAGE);
        auditRepository.save(auditEntry);
    }

    public void writeUpdateStageEvent(StageData stageData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), null, stageData, AuditAction.UPDATE_STAGE);
        auditRepository.save(auditEntry);
    }

    public void writeAddDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.ADD_DOCUMENT);
        auditRepository.save(auditEntry);
    }

    public void writeAddDocumentEvents(List<DocumentData> documentDatum) {
        List<AuditEntry> auditEntries = documentDatum.stream().map(d -> new AuditEntry(requestData.username(), d, AuditAction.ADD_DOCUMENT)).collect(Collectors.toList());
        auditRepository.saveAll(auditEntries);
    }

    public void writeUpdateDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.UPDATE_DOCUMENT);
        auditRepository.save(auditEntry);
    }

    public void writeDeleteDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.DELETE_DOCUMENT);
        auditRepository.save(auditEntry);
    }

    public void writeUndeleteDocumentEvent(DocumentData documentData) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), documentData, AuditAction.UNDELETE_DOCUMENT);
        auditRepository.save(auditEntry);
    }

    public void writeExtractEvent(String params) {
        AuditEntry auditEntry = new AuditEntry(requestData.username(), params, AuditAction.CSV_EXTRACT);
        auditRepository.save(auditEntry);
    }
}
