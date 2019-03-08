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
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.time.LocalDateTime;
import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_TOPIC_DELETED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CASE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.CORRESPONDENT_DELETED;

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

    public static final Set<String> TIMELINE_EVENTS = Set.of(
            CASE_CREATED.toString(),
            CASE_UPDATED.toString(),
            CASE_TOPIC_CREATED.toString(),
            CASE_TOPIC_DELETED.toString(),
            STAGE_ALLOCATED_TO_TEAM.toString(),
            STAGE_ALLOCATED_TO_USER.toString(),
            CORRESPONDENT_DELETED.toString(),
            CORRESPONDENT_CREATED.toString()
            );

    @Autowired
    public AuditClient(ProducerTemplate producerTemplate,
                       @Value("${audit.queue}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData,
                       RestHelper restHelper,
                       @Value("${hocs.audit-service}") String auditService) {
        this.producerTemplate = producerTemplate;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
        this.restHelper = restHelper;
        this.serviceBaseURL = auditService;

    }


    public void updateCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}", caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, CASE_UPDATED);
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
        sendAuditMessage(correspondent.getCaseUUID(), "", CORRESPONDENT_CREATED);
    }

    public void deleteCorrespondentAudit(Correspondent correspondent) {
        sendAuditMessage(correspondent.getCaseUUID(), "", CORRESPONDENT_DELETED);
    }

    public void createTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        sendAuditMessage(caseUUID, "", CASE_TOPIC_CREATED);
    }

    public void deleteTopicAudit(UUID caseUUID, UUID topicNameUUID) {
        sendAuditMessage(caseUUID, "", CASE_TOPIC_DELETED);
    }
    public void createCaseAudit(CaseData caseData) {
        String auditPayload = String.format("{\"reference\":\"%s\"}",  caseData.getReference());
        sendAuditMessage(caseData.getUuid(), auditPayload, CASE_CREATED);
    }

    public void updateStageUser(Stage stage) {
        String auditPayload = String.format("{\"stage\":\"%s\", \"user\":\"%s\"}",  stage.getStageType(), stage.getUserUUID());
        sendAuditMessage(stage.getCaseUUID(), auditPayload, STAGE_ALLOCATED_TO_USER);
    }

    public void updateStageTeam(Stage stage) {
        String auditPayload = String.format("{\"stage\":\"%s\", \"user\":\"%s\"}",  stage.getStageType(), stage.getTeamUUID());
        sendAuditMessage(stage.getCaseUUID(), auditPayload, STAGE_ALLOCATED_TO_TEAM);
    }

    private void sendAuditMessage(UUID caseUUID, String payload, EventType eventType){
        CreateAuditRequest request = new CreateAuditRequest(
                requestData.correlationId(),
                caseUUID,
                raisingService,
                payload,
                namespace,
                LocalDateTime.now(),
                eventType,
                requestData.userId());

        try {
            producerTemplate.sendBodyAndHeaders(auditQueue, objectMapper.writeValueAsString(request), getQueueHeaders());
            log.info("Create audit for Case UUID: {}, correlationID: {}, UserID: {}", caseUUID, requestData.correlationId(), requestData.userId(), value(EVENT, AUDIT_FAILED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {} for reason {}", caseUUID, e, value(EVENT, AUDIT_FAILED));
        }
    }


    public Set<GetAuditResponse> getAuditLinesForCase(UUID caseUUID) {
        try {
            String events = String.join(",", TIMELINE_EVENTS);
            GetAuditListResponse response = restHelper.get(serviceBaseURL, String.format("/audit/case/%s?types=%s", caseUUID, events), GetAuditListResponse.class);
            log.info("Got {} audits", response.getAudits().size(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));
            return response.getAudits();
        } catch (ApplicationExceptions.ResourceException e) {
            log.error("Could not get case types", value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE));
            return new HashSet<>();
        }

    }

    private Map<String, Object> getQueueHeaders() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(RequestData.CORRELATION_ID_HEADER, requestData.correlationId());
        headers.put(RequestData.USER_ID_HEADER, requestData.userId());
        headers.put(RequestData.USERNAME_HEADER, requestData.username());
        headers.put(RequestData.GROUP_HEADER, requestData.groups());
        return headers;
    }
}
