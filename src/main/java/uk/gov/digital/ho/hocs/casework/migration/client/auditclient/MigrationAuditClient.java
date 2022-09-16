package uk.gov.digital.ho.hocs.casework.migration.client.auditclient;

import com.amazonaws.services.sns.AmazonSNSAsync;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.util.SnsStringMessageAttributeValue;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Component
public class MigrationAuditClient {

    private static final String EVENT_TYPE_HEADER = "event_type";

    private final String auditQueue;

    private final String raisingService;

    private final String namespace;

    private final String userId;

    private final String userName;

    private final String group;

    private final AmazonSNSAsync auditSearchSnsClient;

    private final ObjectMapper objectMapper;

    private final RequestData requestData;

    private final RestHelper restHelper;

    private final String serviceBaseURL;

    @Autowired
    public MigrationAuditClient(AmazonSNSAsync auditSearchSnsClient,
                                @Value("${aws.sns.audit-search.arn}") String auditQueue,
                                @Value("${auditing.deployment.name}") String raisingService,
                                @Value("${auditing.deployment.namespace}") String namespace,
                                ObjectMapper objectMapper,
                                RequestData requestData,
                                @Value("${migration.userid}") String userId,
                                @Value("${migration.username}") String userName,
                                @Value("${migration.group}") String group,
                                RestHelper restHelper,
                                @Value("${hocs.audit-service}") String auditService) {
        this.auditSearchSnsClient = auditSearchSnsClient;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.userId = userId;
        this.userName = userName;
        this.group = group;
        this.requestData = requestData;
        this.restHelper = restHelper;
        this.serviceBaseURL = auditService;
    }

    public void updateCaseAudit(CaseData caseData, UUID stageUUID) {
        LocalDateTime localDateTime = LocalDateTime.now();

        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.UpdateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_UPDATED, stageUUID, data,
            requestData.correlationId(), userId, userName, null);

    }

    public void completeCaseAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Case(caseData.getUuid()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_COMPLETED, null, data,
            requestData.correlationId(), userId, userName, null);
    }

    public void createCaseNoteAudit(CaseNote caseNote) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(caseNote);
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseNote.getCaseUUID(), data, EventType.CASE_NOTE_CREATED, null,
            requestData.correlationId(), userId, userName, null);
    }

    public void updateCaseNoteAudit(CaseNote caseNote, String prevCaseNoteType, String prevText) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(
                new AuditPayload.CaseNoteUpdate(prevCaseNoteType, prevText, caseNote.getCaseNoteType(),
                    caseNote.getText()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseNote.getCaseUUID(), data, EventType.CASE_NOTE_UPDATED, null,
            requestData.correlationId(), userId, userName, null);
    }

    public void createCorrespondentAudit(Correspondent correspondent) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCorrespondentRequest.from(correspondent));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, correspondent.getCaseUUID(), data, EventType.CORRESPONDENT_CREATED, null, data,
            requestData.correlationId(), userId, userName, null);
    }

    public void updateCorrespondentAudit(Correspondent correspondent) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCorrespondentRequest.from(correspondent));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, correspondent.getCaseUUID(), data, EventType.CORRESPONDENT_UPDATED, null, data,
            requestData.correlationId(), userId, userName, null);
    }

    public void createTopicAudit(Topic topic) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Topic(topic.getTextUUID(), topic.getText()));
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
        sendAuditMessage(localDateTime, topic.getCaseUUID(), data, EventType.CASE_TOPIC_CREATED, null, data,
            requestData.correlationId(), userId, userName, null);

    }

    public void createCaseAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_CREATED, null, data,
            requestData.correlationId(), userId, userName, group);
    }

    public void createStage(BaseStage stage) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            sendAuditMessage(localDateTime, stage.getCaseUUID(), objectMapper.writeValueAsString(
                    new AuditPayload.StageAllocation(stage.getUuid(), stage.getTeamUUID(), stage.getStageType(),
                        stage.getDeadline(), stage.getDeadlineWarning())), EventType.STAGE_CREATED, stage.getUuid(),
                        requestData.correlationId(), userId, userName, null);
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void updateStageTeam(BaseStage stage) {
        LocalDateTime localDateTime = LocalDateTime.now();

        try {
            EventType allocationType;
            if (stage.getTeamUUID()!=null) {
                allocationType = EventType.STAGE_ALLOCATED_TO_TEAM;
            } else {
                allocationType = EventType.STAGE_COMPLETED;
            }
            sendAuditMessage(localDateTime, stage.getCaseUUID(), objectMapper.writeValueAsString(
                    new AuditPayload.StageAllocation(stage.getUuid(), stage.getTeamUUID(), stage.getStageType(),
                        stage.getDeadline(), stage.getDeadlineWarning())), allocationType, stage.getUuid(),
                        requestData.correlationId(), userId, userName, null);
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    private void sendAuditMessage(LocalDateTime localDateTime,
                                  UUID caseUUID,
                                  String payload,
                                  EventType eventType,
                                  UUID stageUUID,
                                  String correlationId,
                                  String userId,
                                  String username,
                                  String groups) {
        sendAuditMessage(localDateTime, caseUUID, payload, eventType, stageUUID, "{}", correlationId, userId, username,
            groups);
    }

    private void sendAuditMessage(LocalDateTime localDateTime,
                                  UUID caseUUID,
                                  String payload,
                                  EventType eventType,
                                  UUID stageUUID,
                                  String data,
                                  String correlationId,
                                  String userId,
                                  String username,
                                  String group) {
        CreateAuditRequest request = new CreateAuditRequest(correlationId, caseUUID, stageUUID, raisingService, payload,
            data, namespace, localDateTime, eventType, userId);

        try {
            var publishRequest = new PublishRequest(auditQueue,
                objectMapper.writeValueAsString(request)).withMessageAttributes(getQueueHeaders(eventType.toString()));

            auditSearchSnsClient.publish(publishRequest);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}", eventType,
                caseUUID, correlationId, userId, value(LogEvent.EVENT, LogEvent.AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID,
                value(LogEvent.EVENT, LogEvent.AUDIT_FAILED), value(LogEvent.EXCEPTION, e));
        }
    }

    private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(EVENT_TYPE_HEADER, new SnsStringMessageAttributeValue(eventType),
            RequestData.CORRELATION_ID_HEADER, new SnsStringMessageAttributeValue(requestData.correlationId()),
            RequestData.USER_ID_HEADER, new SnsStringMessageAttributeValue(userId),
            RequestData.USERNAME_HEADER, new SnsStringMessageAttributeValue(userName),
            RequestData.GROUP_HEADER, new SnsStringMessageAttributeValue(group));
    }

    private void logFailedToParseAuditPayload(JsonProcessingException e) {
        log.error("Failed to parse audit payload, event {}, exception: {}",
            value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

    private void logFailedToParseDataPayload(JsonProcessingException e) {
        log.error("Failed to parse data payload, event {}, exception: {}",
            value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

}
