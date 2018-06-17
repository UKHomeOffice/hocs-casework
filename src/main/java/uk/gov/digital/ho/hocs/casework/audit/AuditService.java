package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.search.dto.SearchRequest;

import java.util.UUID;

@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
    }

    public void writeSearchEvent(String username, SearchRequest searchRequest) {
        String request = SearchRequest.toJsonString(objectMapper, searchRequest);
        AuditEntry auditEntry = new AuditEntry(username, request, AuditAction.SEARCH);
        auditRepository.save(auditEntry);
    }

    public void writeSendEmailEvent(String username, SendEmailRequest sendEmailRequest) {
        AuditEntry auditEntry = new AuditEntry(username, sendEmailRequest.getEmailAddress(), AuditAction.SEND_EMAIL);
        auditRepository.save(auditEntry);
    }

    public void writeGetCaseEvent(String username, UUID caseUuid) {
        AuditEntry auditEntry = new AuditEntry(username, caseUuid.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeCreateCaseEvent(String username, CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(username, caseData, null, AuditAction.CREATE_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeUpdateCaseEvent(String username, CaseData caseData) {
        AuditEntry auditEntry = new AuditEntry(username, caseData, null, AuditAction.UPDATE_CASE);
        auditRepository.save(auditEntry);
    }

    public void writeCreateStageEvent(String username, StageData stageData) {
        AuditEntry auditEntry = new AuditEntry(username, null, stageData, AuditAction.CREATE_STAGE);
        auditRepository.save(auditEntry);
    }

    public void writeExtractEvent(String username, String params) {
        AuditEntry auditEntry = new AuditEntry(username, params, AuditAction.CSV_EXTRACT);
        auditRepository.save(auditEntry);
    }

    public void writeUpdateStageEvent(String username, StageData stageData) {
        AuditEntry auditEntry = new AuditEntry(username, null, stageData, AuditAction.UPDATE_STAGE);
        auditRepository.save(auditEntry);
    }
}
