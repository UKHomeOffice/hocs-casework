package uk.gov.digital.ho.hocs.casework.client.auditclient;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import software.amazon.awssdk.services.sns.SnsAsyncClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.AuditPayload;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.CreateAuditRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.DeleteCaseAuditDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.DeleteCaseAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditListResponse;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeActionDto;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataDeadlineExtension;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataExternalInterest;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataSuspension;
import uk.gov.digital.ho.hocs.casework.domain.model.BaseStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.api.dto.AppealOfficerDto.OfficerData;

@Slf4j
@Component
public class AuditClient {

    private static final String EVENT_TYPE_HEADER = "event_type";

    private final String auditQueue;

    private final String raisingService;

    private final String namespace;

    private final SnsAsyncClient auditSearchSnsClient;

    private final ObjectMapper objectMapper;

    private final RequestData requestData;

    private final RestHelper restHelper;

    private final String serviceBaseURL;

    @Autowired
    public AuditClient(SnsAsyncClient auditSearchSnsClient,
                       @Value("${aws.sns.audit-search.arn}") String auditQueue,
                       @Value("${auditing.deployment.name}") String raisingService,
                       @Value("${auditing.deployment.namespace}") String namespace,
                       ObjectMapper objectMapper,
                       RequestData requestData,
                       RestHelper restHelper,
                       @Value("${hocs.audit-service}") String auditService) {
        this.auditSearchSnsClient = auditSearchSnsClient;
        this.auditQueue = auditQueue;
        this.raisingService = raisingService;
        this.namespace = namespace;
        this.objectMapper = objectMapper;
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());

    }

    public void viewCaseAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();

        try {
            sendAuditMessage(localDateTime, caseData.getUuid(),
                objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())),
                EventType.CASE_VIEWED, null, requestData.correlationId(), requestData.userId(), requestData.username(),
                requestData.groups());
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void viewStandardLineAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            sendAuditMessage(localDateTime, caseData.getUuid(),
                objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())),
                EventType.STANDARD_LINE_VIEWED, null, requestData.correlationId(), requestData.userId(),
                requestData.username(), requestData.groups());
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void viewTemplateAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            sendAuditMessage(localDateTime, caseData.getUuid(),
                objectMapper.writeValueAsString(new AuditPayload.CaseReference(caseData.getReference())),
                EventType.TEMPLATE_VIEWED, null, requestData.correlationId(), requestData.userId(),
                requestData.username(), requestData.groups());
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void deleteCaseAudit(CaseData caseData, Boolean deleted) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.CaseDeleted(caseData.getUuid(), deleted));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_DELETED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void viewCaseNotesAudit(UUID caseUUID) {
        LocalDateTime localDateTime = LocalDateTime.now();
        sendAuditMessage(localDateTime, caseUUID, "", EventType.CASE_NOTES_VIEWED, null, requestData.correlationId(),
            requestData.userId(), requestData.username(), requestData.groups());
    }

    public void viewCaseNoteAudit(CaseNote caseNote) {
        LocalDateTime localDateTime = LocalDateTime.now();
        sendAuditMessage(localDateTime, caseNote.getCaseUUID(), "", EventType.CASE_NOTE_VIEWED, null,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void deleteCaseNoteAudit(CaseNote caseNote) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(caseNote);
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseNote.getCaseUUID(), data, EventType.CASE_NOTE_DELETED, null,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void viewAllSomuItemsAudit(UUID caseUUID) {
        LocalDateTime localDateTime = LocalDateTime.now();
        sendAuditMessage(localDateTime, caseUUID, "", EventType.SOMU_ITEMS_VIEWED, null, requestData.correlationId(),
            requestData.userId(), requestData.username(), requestData.groups());
    }

    public void viewCaseSomuItemsBySomuTypeAudit(UUID caseUUID, UUID somuTypeUUID) {
        LocalDateTime localDateTime = LocalDateTime.now();

        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.SomuItem(somuTypeUUID));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }

        sendAuditMessage(localDateTime, caseUUID, data, EventType.SOMU_ITEM_VIEWED, null, requestData.correlationId(),
            requestData.userId(), requestData.username(), requestData.groups());
    }

    public void createCaseSomuItemAudit(SomuItem somuItem) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(
                new AuditPayload.SomuItemWithData(somuItem.getSomuUuid(), somuItem.getUuid(), somuItem.getData()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, somuItem.getCaseUuid(), data, EventType.SOMU_ITEM_CREATED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void updateSomuItemAudit(SomuItem somuItem) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(
                new AuditPayload.SomuItemWithData(somuItem.getSomuUuid(), somuItem.getUuid(), somuItem.getData()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, somuItem.getCaseUuid(), data, EventType.SOMU_ITEM_UPDATED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void deleteSomuItemAudit(SomuItem somuItem) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(
                new AuditPayload.SomuItemWithData(somuItem.getSomuUuid(), somuItem.getUuid(), somuItem.getData()));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, somuItem.getCaseUuid(), data, EventType.SOMU_ITEM_DELETED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void createExtensionAudit(ActionDataDeadlineExtension actionDataDeadlineExtension) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(
                AuditPayload.CreateExtensionRequest.from(actionDataDeadlineExtension));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, actionDataDeadlineExtension.getCaseDataUuid(), data,
            EventType.EXTENSION_APPLIED, null, data, requestData.correlationId(), requestData.userId(),
            requestData.username(), requestData.groups());
    }

    public void createAppealAudit(ActionDataAppeal appealEntity, CaseTypeActionDto caseTypeActionDto) {
        LocalDateTime localDateTime = LocalDateTime.now();

        AuditPayload.AppealItem.AppealItemBuilder appealAuditItemBuilder = AuditPayload.AppealItem.builder().caseTypeActionUuid(
            appealEntity.getCaseTypeActionUuid()).complexCase(appealEntity.getComplexCase()).created(
            appealEntity.getCreateTimestamp()).dateSentRMS(appealEntity.getDateSentRMS()).status(
            appealEntity.getStatus()).outcome(appealEntity.getOutcome()).note(appealEntity.getNote());

        String data = "{}";
        try {
            addAppealOfficerInfo(appealAuditItemBuilder, appealEntity, caseTypeActionDto);
            data = objectMapper.writeValueAsString(appealAuditItemBuilder.build());
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, appealEntity.getCaseDataUuid(), data, EventType.APPEAL_CREATED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());

    }

    public void updateAppealAudit(ActionDataAppeal appealEntity, CaseTypeActionDto caseTypeActionDto) {
        LocalDateTime localDateTime = LocalDateTime.now();

        AuditPayload.AppealItem.AppealItemBuilder appealAuditItemBuilder = AuditPayload.AppealItem.builder().caseTypeActionUuid(
            appealEntity.getCaseTypeActionUuid()).complexCase(appealEntity.getComplexCase()).created(
            appealEntity.getCreateTimestamp()).dateSentRMS(appealEntity.getDateSentRMS()).status(
            appealEntity.getStatus()).outcome(appealEntity.getOutcome()).note(appealEntity.getNote());

        String data = "{}";
        try {
            addAppealOfficerInfo(appealAuditItemBuilder, appealEntity, caseTypeActionDto);
            data = objectMapper.writeValueAsString(appealAuditItemBuilder.build());
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, appealEntity.getCaseDataUuid(), data, EventType.APPEAL_UPDATED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());

    }

    public void updateExternalInterestAudit(ActionDataExternalInterest actionDataExternalInterest) {
        saveActionAudit(new AuditPayload.ExternalInterestItem(actionDataExternalInterest.getUuid(),
            actionDataExternalInterest.getCaseDataUuid(), actionDataExternalInterest.getCaseDataType(),
            actionDataExternalInterest.getDetailsOfInterest(), actionDataExternalInterest.getPartyType(),
            EventType.EXTERNAL_INTEREST_UPDATED));
    }

    public void createExternalInterestAudit(ActionDataExternalInterest actionDataExternalInterest) {
        saveActionAudit(new AuditPayload.ExternalInterestItem(actionDataExternalInterest.getUuid(),
            actionDataExternalInterest.getCaseDataUuid(), actionDataExternalInterest.getCaseDataType(),
            actionDataExternalInterest.getDetailsOfInterest(), actionDataExternalInterest.getPartyType(),
            EventType.EXTERNAL_INTEREST_CREATED));
    }

    private void saveActionAudit(AuditPayload.ActionAuditPayload actionAuditPayload) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(actionAuditPayload);
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, actionAuditPayload.getCaseDataUuid(), data, actionAuditPayload.getEventType(),
            null, data, requestData.correlationId(), requestData.userId(), requestData.username(),
            requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void deleteCorrespondentAudit(Correspondent correspondent) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCorrespondentRequest.from(correspondent));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, correspondent.getCaseUUID(), data, EventType.CORRESPONDENT_DELETED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());

    }

    public void deleteTopicAudit(Topic topic) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(new AuditPayload.Topic(topic.getTextUUID(), topic.getText()));
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
        sendAuditMessage(localDateTime, topic.getCaseUUID(), data, EventType.CASE_TOPIC_DELETED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void migrateCaseAudit(CaseData caseData) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String data = "{}";
        try {
            data = objectMapper.writeValueAsString(AuditPayload.CreateCaseRequest.from(caseData));
        } catch (JsonProcessingException e) {
            logFailedToParseDataPayload(e);
        }
        sendAuditMessage(localDateTime, caseData.getUuid(), data, EventType.CASE_MIGRATED, null, data,
            requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
    }

    public void updateStageUser(BaseStage stage) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            EventType allocationType;
            if (stage.getUserUUID()!=null) {
                allocationType = EventType.STAGE_ALLOCATED_TO_USER;
            } else {
                allocationType = EventType.STAGE_UNALLOCATED_FROM_USER;
            }
            sendAuditMessage(localDateTime, stage.getCaseUUID(), objectMapper.writeValueAsString(
                    new AuditPayload.StageAllocation(stage.getUuid(), stage.getUserUUID(), stage.getStageType(),
                        stage.getDeadline(), stage.getDeadlineWarning())), allocationType, stage.getUuid(),
                requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void createStage(BaseStage stage) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            sendAuditMessage(localDateTime, stage.getCaseUUID(), objectMapper.writeValueAsString(
                    new AuditPayload.StageAllocation(stage.getUuid(), stage.getTeamUUID(), stage.getStageType(),
                        stage.getDeadline(), stage.getDeadlineWarning())), EventType.STAGE_CREATED, stage.getUuid(),
                requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
        } catch (JsonProcessingException e) {
            logFailedToParseAuditPayload(e);
        }
    }

    public void recreateStage(BaseStage stage) {
        LocalDateTime localDateTime = LocalDateTime.now();
        try {
            sendAuditMessage(localDateTime, stage.getCaseUUID(), objectMapper.writeValueAsString(
                    new AuditPayload.StageAllocation(stage.getUuid(), stage.getTeamUUID(), stage.getStageType(), null,
                        null)), EventType.STAGE_RECREATED, stage.getUuid(), requestData.correlationId(),
                requestData.userId(), requestData.username(), requestData.groups());
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
                requestData.correlationId(), requestData.userId(), requestData.username(), requestData.groups());
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
                                  String groups) {
        CreateAuditRequest request = new CreateAuditRequest(correlationId, caseUUID, stageUUID, raisingService, payload,
            data, namespace, localDateTime, eventType, userId);

        try {
            var publishRequest =  PublishRequest.builder()
                .topicArn(auditQueue)
                .message(objectMapper.writeValueAsString(request))
                .messageAttributes(getQueueHeaders(eventType.toString()))
                .build();

            auditSearchSnsClient.publish(publishRequest);
            log.info("Create audit of type {} for Case UUID: {}, correlationID: {}, UserID: {}, event: {}", eventType,
                caseUUID, correlationId, userId, value(LogEvent.EVENT, LogEvent.AUDIT_EVENT_CREATED));
        } catch (Exception e) {
            log.error("Failed to create audit event for case UUID {}, event {}, exception: {}", caseUUID,
                value(LogEvent.EVENT, LogEvent.AUDIT_FAILED), value(LogEvent.EXCEPTION, e));
        }
    }

    public Set<GetAuditResponse> getAuditLinesForCase(UUID caseUUID, List<String> requestedEvents) {
        try {
            String events = String.join(",", requestedEvents);
            GetAuditListResponse response = restHelper.get(serviceBaseURL,
                String.format("/audit/case/%s?types=%s", caseUUID, events), GetAuditListResponse.class);
            log.info("Got {} audits", response.getAudits().size(),
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));
            return response.getAudits();
        } catch (RestClientException e) {
            log.error("Could not get audit lines, event {}, exception: {}",
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(LogEvent.EXCEPTION, e));
            return new HashSet<>();
        }
    }

    public Set<GetAuditResponse> getAuditLinesForCase(UUID caseUUID, LocalDate fromDate, List<String> requestedEvents) {
        try {
            String events = String.join(",", requestedEvents);
            GetAuditListResponse response = restHelper.get(serviceBaseURL,
                String.format("/audit/case/%s?fromDate=%s&types=%s", caseUUID, fromDate, events),
                GetAuditListResponse.class);
            log.info("Got {} audits", response.getAudits().size(),
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS));
            return response.getAudits();
        } catch (RestClientException e) {
            log.error("Could not get audit lines, event {}, exception: {}",
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE), value(LogEvent.EXCEPTION, e));
            return new HashSet<>();
        }
    }

    public DeleteCaseAuditResponse deleteAuditLinesForCase(UUID caseUUID, String correlationId, Boolean deleted) {
        try {
            DeleteCaseAuditDto deleteCaseAuditDto = new DeleteCaseAuditDto(correlationId, deleted);
            DeleteCaseAuditResponse response = restHelper.post(serviceBaseURL,
                String.format("/audit/case/%s/delete", caseUUID), deleteCaseAuditDto, DeleteCaseAuditResponse.class);
            log.info("Deleted {} audits for Case {}", response.getAuditCount(), caseUUID,
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_SUCCESS));
            return response;
        } catch (RestClientException e) {
            log.error("Could not delete audit lines, event {}, exception: {}",
                value(LogEvent.EVENT, LogEvent.AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_FAILURE),
                value(LogEvent.EXCEPTION, e));
            return new DeleteCaseAuditResponse(correlationId, caseUUID, deleted, 0);
        }
    }

    private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {
        return Map.of(EVENT_TYPE_HEADER, MessageAttributeValue.builder().dataType("String").stringValue(eventType).build(),
            RequestData.CORRELATION_ID_HEADER, MessageAttributeValue.builder().stringValue(requestData.correlationId()).build(),
            RequestData.USER_ID_HEADER, MessageAttributeValue.builder().stringValue(requestData.userId()).build(),
            RequestData.USERNAME_HEADER, MessageAttributeValue.builder().stringValue(requestData.username()).build(),
            RequestData.GROUP_HEADER, MessageAttributeValue.builder().stringValue(requestData.groups()).build());
    }

    private void logFailedToParseAuditPayload(JsonProcessingException e) {
        log.error("Failed to parse audit payload, event {}, exception: {}",
            value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

    private void logFailedToParseDataPayload(JsonProcessingException e) {
        log.error("Failed to parse data payload, event {}, exception: {}",
            value(LogEvent.EVENT, LogEvent.UNCAUGHT_EXCEPTION), value(LogEvent.EXCEPTION, e));
    }

    public void addAppealOfficerInfo(AuditPayload.AppealItem.AppealItemBuilder builder,
                                     ActionDataAppeal existingAppealData,
                                     CaseTypeActionDto caseTypeActionDto) throws JsonProcessingException {
        if (existingAppealData.getAppealOfficerData()==null) {
            log.warn("No officer data exists for for case action type: {}", caseTypeActionDto.getActionLabel());
            return;
        }

        try {
            OfficerData officerDetailsSchema = objectMapper.readValue(caseTypeActionDto.getProps(), OfficerData.class);
            Map<String, String> appealOfficerDataMap = objectMapper.readValue(existingAppealData.getAppealOfficerData(),
                new TypeReference<>() {});

            if (appealOfficerDataMap==null || officerDetailsSchema==null) {
                return;
            }

            var appealOfficerData = officerDetailsSchema.getAppealOfficerData();
            if (appealOfficerData==null) {
                return;
            }

            var appealOfficerName = appealOfficerData.getOfficer();
            var appealOfficerDirectorate = appealOfficerData.getDirectorate();
            if (appealOfficerName==null || appealOfficerDirectorate==null) {
                return;
            }

            builder.officerType(appealOfficerName.getValue()).officerName(
                appealOfficerDataMap.get(appealOfficerName.getValue())).officerDirectorate(
                appealOfficerDataMap.get(appealOfficerDirectorate.getValue()));
        } catch (JsonProcessingException e) {
            log.warn("Failed to map appeal officer data for action type: {}", caseTypeActionDto.getActionLabel());
            throw e;
        }
    }

    public void suspendCaseAudit(ActionDataSuspension suspensionEntity) {
        AuditPayload.ActionAuditPayload payload = getActionAuditPayload(suspensionEntity,
            EventType.CASE_SUSPENSION_APPLIED);
        saveActionAudit(payload);
    }

    public void unsuspendCaseAudit(ActionDataSuspension suspensionEntity) {
        AuditPayload.ActionAuditPayload payload = getActionAuditPayload(suspensionEntity,
            EventType.CASE_SUSPENSION_REMOVED);
        saveActionAudit(payload);
    }

    private AuditPayload.ActionAuditPayload getActionAuditPayload(ActionDataSuspension suspensionEntity,
                                                                  EventType suspensionEventType) {
        return new AuditPayload.SuspensionItem(suspensionEntity.getUuid(), suspensionEntity.getCaseTypeActionUuid(),
            suspensionEntity.getActionSubtype(), suspensionEntity.getCaseTypeActionLabel(),
            suspensionEntity.getCaseDataType(), suspensionEntity.getCaseDataUuid(),
            suspensionEntity.getDateSuspensionApplied(), suspensionEntity.getDateSuspensionRemoved(),
            suspensionEventType);
    }

}
