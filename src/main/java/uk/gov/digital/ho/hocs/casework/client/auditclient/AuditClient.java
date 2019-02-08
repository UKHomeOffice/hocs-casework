package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Component
public class AuditClient {

    private final String auditQueue;
    private final String raisingService;
    private final String namespace;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;

    private final RequestData requestData;

    private final RestHelper restHelper;
    private final String serviceBaseURL;


    @Autowired
    public AuditClient(ProducerTemplate producerTemplate,
                       @Value("${audit.queue}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData,
                       RestHelper restHelper,
                       @Value("${hocs.audit-service}") String infoService) {
        this.producerTemplate = producerTemplate;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
        this.restHelper = restHelper;
        this.serviceBaseURL = infoService;

    }

    public void createCaseAudit(UUID caseUUID, UUID stageUUID, String reference) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", reference);
        sendAuditMessage(caseUUID, stageUUID, auditPayload, EventType.CASE_CREATED);
    }

    public void updateCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_UPDATED);
    }

    public void viewCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_VIEWED);
    }

    public void viewCaseSummaryAudit(CaseData caseData, CaseSummary caseSummary) {
        String auditPayload = String.format("{\"reference\":\"%s\"}",  caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_SUMMARY_VIEWED);
    }

    public void viewStandardLineAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.STANDARD_LINE_VIEWED);
    }

    public void viewTemplateAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.TEMPLATE_VIEWED);
    }

    public void deleteCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_DELETED);
    }

    public void viewCaseNotesAudit(UUID caseUUID, Set<CaseNote> caseNotes) {
        sendAuditMessage(caseUUID, "", EventType.CASE_NOTES_VIEWED);
    }

    public void viewCaseNoteAudit(CaseNote caseNote) {
        sendAuditMessage(caseNote.getCaseUUID(), "", EventType.CASE_NOTE_VIEWED);
    }

    public void createCaseNoteAudit(CaseNote caseNote) {
        sendAuditMessage(caseNote.getCaseUUID(), "", EventType.CASE_NOTE_CREATED);
    }

    public void createCorrespondentAudit(Correspondent correspondent) {
        sendAuditMessage(correspondent.getCaseUUID(), "", EventType.CORRESPONDENT_CREATED);
    }

    public void deleteCorrespondentAudit(Correspondent correspondent) {
        sendAuditMessage(correspondent.getCaseUUID(), "", EventType.CORRESPONDENT_DELETED);
    }

    public void createTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        sendAuditMessage(caseUUID, "", EventType.CASE_TOPIC_CREATED);
    }

    public void deleteTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        sendAuditMessage(caseUUID, "", EventType.CASE_TOPIC_DELETED);
    }

    public void updateStageUser(Stage stage) {
        String auditPayload = String.format("{\"stage\":\"%s\", \"user\":\"%s\"}",  stage.getStageType(), stage.getUserUUID());
        sendAuditMessage(stage.getCaseUUID(), "", EventType.STAGE_ALLOCATED_TO);
    }

    public void createCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}",  caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_CREATED);
    }
  
    private void sendAuditMessage(UUID caseUUID, UUID stageUUID, String payload, EventType eventType){
        CreateAuditRequest request = new CreateAuditRequest(
                requestData.correlationId(),
                caseUUID,
                stageUUID,
                raisingService,
                payload,
                namespace,
                LocalDateTime.now(),
                eventType,
                requestData.userId());

        try {
            producerTemplate.sendBody(auditQueue, objectMapper.writeValueAsString(request));
            log.info("Create audit for Case UUID: {}, correlationID: {}, UserID: {}", caseUUID, requestData.correlationId(), requestData.userId(), value(EVENT, AUDIT_FAILED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {} for reason {}", caseUUID, e, value(EVENT, AUDIT_FAILED));

    }

    public Set<GetAuditResponse> getAuditLinesForCase(UUID caseUUID) {
        try {
            //TODO: this list should be in info service?
            String events = String.join(",", EventType.CASE_CREATED.toString());
            GetAuditListResponse response = restHelper.get(serviceBaseURL, String.format("/audit/case/%s?types=%s", caseUUID, events), GetAuditListResponse.class);
            log.info("Got {} audits", response.getAudits().size(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));
            return response.getAudits();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case types", value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE));
            throw new ApplicationExceptions.EntityNotFoundException("Could not get case types", AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE);
        }

    }
}
