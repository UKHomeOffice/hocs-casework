package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCorrespondentResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
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

    @Autowired
    public AuditClient(ProducerTemplate producerTemplate,
                       @Value("${audit.queue}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData) {
        this.producerTemplate = producerTemplate;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    public void updateCaseAudit(CaseData caseData) throws JsonProcessingException {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        GetCaseResponse data = GetCaseResponse.from(caseData);
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_UPDATED, objectMapper.writeValueAsString(data));
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
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.STANDARD_LINE_VIEWED );
    }

    public void viewTemplateAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.TEMPLATE_VIEWED);
    }

    public void deleteCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        String data = String.format("{\"uuid\":\"%s\"}", caseData.getUuid());
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_DELETED, data);
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

    public void createCorrespondentAudit(Correspondent correspondent) throws JsonProcessingException {
        GetCorrespondentResponse data = GetCorrespondentResponse.from(correspondent);
        sendAuditMessage(correspondent.getCaseUUID(), "", EventType.CORRESPONDENT_CREATED, objectMapper.writeValueAsString(data));
    }

    public void deleteCorrespondentAudit(Correspondent correspondent) {
        String data = String.format("{\"uuid\":\"%s\"}",  correspondent.getUuid());
        sendAuditMessage(correspondent.getCaseUUID(), "", EventType.CORRESPONDENT_DELETED, data);
    }

    public void createTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        String data = String.format("{\"uuid\":\"%s\"}",  topicNameUUID);
        sendAuditMessage(caseUUID, "", EventType.CASE_TOPIC_CREATED, data);
    }

    public void deleteTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        String data = String.format("{\"uuid\":\"%s\"}",  topicNameUUID);
        sendAuditMessage(caseUUID, "", EventType.CASE_TOPIC_DELETED, data);
    }

    public void updateStageUser(Stage stage) {
        String auditPayload = String.format("{\"stage\":\"%s\", \"user\":\"%s\"}",  stage.getStageType(), stage.getUserUUID());
        sendAuditMessage(stage.getCaseUUID(), auditPayload, EventType.STAGE_ALLOCATED_TO_USER, auditPayload);
    }

    public void updateStageTeam(Stage stage) {
        String auditPayload = String.format("{\"stage\":\"%s\", \"team\":\"%s\"}",  stage.getStageType(), stage.getTeamUUID());
        sendAuditMessage(stage.getCaseUUID(), auditPayload, EventType.STAGE_ALLOCATED_TO_TEAM, auditPayload);
    }

    public void createCaseAudit(CaseData caseData) throws JsonProcessingException {
        String auditPayload = String.format("{\"reference\":\"%s\"}",  caseData.getReference());
        CreateCaseResponse data = CreateCaseResponse.from(caseData);
        sendAuditMessage(caseData.getUuid(), auditPayload, EventType.CASE_CREATED, objectMapper.writeValueAsString(data));
    }

    private void sendAuditMessage(UUID caseUUID, String payload, EventType eventType, String data){
        CreateAuditRequest request = new CreateAuditRequest(
                requestData.correlationId(),
                caseUUID,
                raisingService,
                payload,
                data,
                namespace,
                LocalDateTime.now(),
                eventType,
                requestData.userId());
        sendMessage(request);
    }

    private void sendAuditMessage(UUID caseUUID, String payload, EventType eventType) {
        sendAuditMessage(caseUUID, payload, eventType, "");
    }

    private void sendMessage(CreateAuditRequest request) {
        try {
            producerTemplate.sendBody(auditQueue, objectMapper.writeValueAsString(request));
            log.info("Create audit for Case UUID: {}, correlationID: {}, UserID: {}", request.getCaseUUID(), requestData.correlationId(), requestData.userId(), value(EVENT, AUDIT_FAILED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {} for reason {}", request.getCaseUUID(), e, value(EVENT, AUDIT_FAILED));
        }
    }

}
