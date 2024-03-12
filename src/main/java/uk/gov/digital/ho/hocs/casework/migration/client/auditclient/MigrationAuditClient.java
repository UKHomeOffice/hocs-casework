package uk.gov.digital.ho.hocs.casework.migration.client.auditclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.auditclient.EventType;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
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

    private final SnsAsyncClient auditSearchSnsClient;

    private final ObjectMapper objectMapper;

    private final RequestData requestData;

    @Autowired
    public MigrationAuditClient(SnsAsyncClient auditSearchSnsClient,
                                @Value("${aws.sns.audit-search.arn}") String auditQueue,
                                @Value("${auditing.deployment.name}") String raisingService,
                                @Value("${auditing.deployment.namespace}") String namespace,
                                ObjectMapper objectMapper,
                                RequestData requestData,
                                @Value("${migration.userid}") String userId,
                                @Value("${migration.username}") String userName,
                                @Value("${migration.group}") String group) {
        this.auditSearchSnsClient = auditSearchSnsClient;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
        this.userId = userId;
        this.userName = userName;
        this.group = group;
        this.requestData = requestData;
    }

    public void updateCaseAudit(CaseData caseData, UUID stageUUID) {
        updateCaseAudit(caseData, stageUUID, caseData.getCreated());
    }

    public void updateCaseAudit(CaseData caseData, UUID stageUUID, LocalDateTime auditEventTimestamp) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.UpdateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(auditEventTimestamp, caseData.getUuid(), data, EventType.CASE_UPDATED, stageUUID, data,
            requestData.correlationId(), userId);
    }

    public void completeCaseAudit(CaseData caseData) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Case(caseData.getUuid()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(caseData.getDateCompleted(), caseData.getUuid(), data, EventType.CASE_COMPLETED, null, data,
            requestData.correlationId(), userId);
    }

    public void createCaseAudit(CaseData caseData) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(caseData.getCreated(), caseData.getUuid(), data, EventType.CASE_CREATED, null, data,
            requestData.correlationId(), userId);
    }

    public void createCorrespondentAudit(CaseData caseData, Correspondent correspondent) {
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCorrespondentRequest.from(correspondent));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(caseData.getCreated(), correspondent.getCaseUUID(), data, EventType.CORRESPONDENT_CREATED, null, data,
            requestData.correlationId(), requestData.userId());
    }

    private void sendAuditMessage(LocalDateTime localDateTime,
                                  UUID caseUUID,
                                  String payload,
                                  EventType eventType,
                                  UUID stageUUID,
                                  String data,
                                  String correlationId,
                                  String userId) {
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

    private Map<String, SnsStringMessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(EVENT_TYPE_HEADER, new SnsStringMessageAttributeValue(eventType),
            RequestData.CORRELATION_ID_HEADER, new SnsStringMessageAttributeValue(requestData.correlationId()),
            RequestData.USER_ID_HEADER, new SnsStringMessageAttributeValue(userId),
            RequestData.USERNAME_HEADER, new SnsStringMessageAttributeValue(userName),
            RequestData.GROUP_HEADER, new SnsStringMessageAttributeValue(group));
    }

    private void logFailedToParseDataPayload(JsonProcessingException e) {
        log.error("Failed to parse data payload, event {}, exception: {}",
            value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

}
