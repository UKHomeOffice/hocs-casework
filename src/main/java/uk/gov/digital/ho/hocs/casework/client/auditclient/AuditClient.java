package uk.gov.digital.ho.hocs.casework.client.auditclient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
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
    private final String EVENT_TYPE_HEADER ="event_type";


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

    @Async
    public void updateCaseAudit(CaseData caseData, UUID stageUUID)  {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.UpdateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse data payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(caseData.getUuid(), data, CASE_UPDATED, stageUUID, data);
    }

    @Async
    public void viewCaseAudit(CaseData caseData)  {
        try {
            sendAuditMessage(caseData.getUuid(), objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())), EventType.CASE_VIEWED, null);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Async
    public void viewCaseSummaryAudit(CaseData caseData, CaseSummary caseSummary)  {
        try {
            sendAuditMessage(caseData.getUuid(), objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())), EventType.CASE_SUMMARY_VIEWED, null);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Async
    public void viewStandardLineAudit(CaseData caseData)  {
        try {
            sendAuditMessage(caseData.getUuid(), objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())), EventType.STANDARD_LINE_VIEWED, null);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Async
    public void viewTemplateAudit(CaseData caseData)  {
        try {
            sendAuditMessage(caseData.getUuid(), objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())), EventType.TEMPLATE_VIEWED, null);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Async
    public void deleteCaseAudit(CaseData caseData)  {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Case(caseData.getUuid()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse data payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(caseData.getUuid(), data, EventType.CASE_DELETED, null,data);
    }

    @Async
    public void viewCaseNotesAudit(UUID caseUUID, Set<CaseNote> caseNotes) {
        sendAuditMessage(caseUUID, "", EventType.CASE_NOTES_VIEWED, null);
    }

    @Async
    public void viewCaseNoteAudit(CaseNote caseNote) {
        sendAuditMessage(caseNote.getCaseUUID(), "", EventType.CASE_NOTE_VIEWED,null);
    }

    @Async
    public void createCaseNoteAudit(CaseNote caseNote) {
        sendAuditMessage(caseNote.getCaseUUID(), "", EventType.CASE_NOTE_CREATED, null);
    }

    @Async
    public void createCorrespondentAudit(Correspondent correspondent) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCorrespondentRequest.from(correspondent));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse data payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(correspondent.getCaseUUID(), data, CORRESPONDENT_CREATED, null, data);
    }

    @Async
    public void deleteCorrespondentAudit(Correspondent correspondent) {
        String data = String.format("{\"uuid\":\"%s\"}",  correspondent.getUuid());
        sendAuditMessage(correspondent.getCaseUUID(), "", CORRESPONDENT_DELETED, null, data);
    }

    @Async
    public void createTopicAudit(Topic topic)  {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Topic(topic.getTextUUID(), topic.getText()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(topic.getCaseUUID(),data, CASE_TOPIC_CREATED, null, data);

    }

    @Async
    public void deleteTopicAudit(Topic topic) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Topic(topic.getTextUUID(), topic.getText()));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(topic.getCaseUUID(), data, CASE_TOPIC_DELETED, null, data);
    }

    @Async
    public void createCaseAudit(CaseData caseData)  {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            log.error("Failed to parse data payload", UNCAUGHT_EXCEPTION);
        }
        sendAuditMessage(caseData.getUuid(), data, CASE_CREATED, null, data);
    }

    @Async
    public void updateStageUser(Stage stage)  {
        try {
            EventType allocationType;
            if(stage.getUserUUID() != null) {
                allocationType = STAGE_ALLOCATED_TO_USER;
            }
            else {
                allocationType = STAGE_UNALLOCATED_FROM_USER;
            }
            sendAuditMessage(stage.getCaseUUID(), objectMapper.writeValueAsString(new AuditPayload.StageUserAllocation(stage.getUuid(), stage.getUserUUID(), stage.getStageType())), allocationType, stage.getUuid());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Async
    public void updateStageTeam(Stage stage)  {
        try {
            EventType allocationType;
            if(stage.getTeamUUID() != null) {
                allocationType = STAGE_ALLOCATED_TO_TEAM;
            }
            else {
                allocationType = STAGE_UNALLOCATED_FROM_TEAM;
            }
            sendAuditMessage(stage.getCaseUUID(), objectMapper.writeValueAsString(new AuditPayload.StageTeamAllocation(stage.getUuid(), stage.getTeamUUID(),stage.getStageType())), allocationType, stage.getUuid());
        } catch (JsonProcessingException e) {
            log.error("Failed to parse audit payload", UNCAUGHT_EXCEPTION);
        }
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    private void sendAuditMessage(UUID caseUUID, String payload, EventType eventType, UUID stageUUID, String data){
        CreateAuditRequest request = new CreateAuditRequest(
                requestData.correlationId(),
                caseUUID,
                stageUUID,
                raisingService,
                payload,
                data,
                namespace,
                LocalDateTime.now(),
                eventType,
                requestData.userId());

        try {
            Map<String, Object> queueHeaders = getQueueHeaders(eventType.toString());
            producerTemplate.sendBodyAndHeaders(auditQueue, objectMapper.writeValueAsString(request), queueHeaders);
            log.info("Create audit for Case UUID: {}, correlationID: {}, UserID: {}", caseUUID, requestData.correlationId(), requestData.userId(), value(EVENT, AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {} for reason {}", caseUUID, e, value(EVENT, AUDIT_FAILED));
        }
    }

    private void sendAuditMessage(UUID caseUUID, String payload, EventType eventType, UUID stageUUID) {
        sendAuditMessage(caseUUID, payload, eventType, stageUUID, "{}");
    }

    public Set<GetAuditResponse> getAuditLinesForCase(UUID caseUUID, Set<String> requestedEvents) {
        try {
            String events = String.join(",", requestedEvents);
            GetAuditListResponse response = restHelper.get(serviceBaseURL, String.format("/audit/case/%s?types=%s", caseUUID, events), GetAuditListResponse.class);
            log.info("Got {} audits", response.getAudits().size(), value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));
            return response.getAudits();
        } catch (RestClientException e) {
            log.error("Could not get audit lines", value(EVENT, AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE));
            return new HashSet<>();
        }
    }

    private Map<String, Object> getQueueHeaders(String eventType) {
        return Map.of(
        EVENT_TYPE_HEADER, eventType,
        RequestData.CORRELATION_ID_HEADER, requestData.correlationId(),
        RequestData.USER_ID_HEADER, requestData.userId(),
        RequestData.USERNAME_HEADER, requestData.username(),
        RequestData.GROUP_HEADER, requestData.groups());
    }
}
