package uk.gov.digital.ho.hocs.casework.client.notifyclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.NotifyCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.OfflineQaUserCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.TeamAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.UserAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.util.SqsStringMessageAttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;

@Slf4j
@Component
public class NotifyClient {

    private static final String EVENT_TYPE_HEADER = "event_type";

    private final String notifyQueueUrl;

    private final SqsAsyncClient notifyAsyncClient;

    private final ObjectMapper objectMapper;

    private final RequestData requestData;

    @Autowired
    public NotifyClient(SqsAsyncClient notifyAsyncClient,
                        @Value("${aws.sqs.notify.url}") String notifyQueueUrl,
                        ObjectMapper objectMapper,
                        RequestData requestData) {
        this.notifyAsyncClient = notifyAsyncClient;
        this.notifyQueueUrl = notifyQueueUrl;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    public void sendTeamEmail(UUID caseUUID, UUID stageUUID, UUID teamUUID, String caseReference, String emailType) {
        sendMessage(new TeamAssignChangeCommand(caseUUID, stageUUID, caseReference, teamUUID, emailType),
            LogEvent.TEAM_EMAIL_SENT);
    }

    public void sendUserEmail(UUID caseUUID,
                              UUID stageUUID,
                              UUID currentUserUUID,
                              UUID newUserUUID,
                              String caseReference) {
        sendMessage(new UserAssignChangeCommand(caseUUID, stageUUID, caseReference, currentUserUUID, newUserUUID),
            LogEvent.USER_EMAIL_SENT);
    }

    public void sendOfflineQaEmail(UUID caseUUID,
                                   UUID stageUUID,
                                   UUID currentUserUUID,
                                   UUID offlineUserUUID,
                                   String caseReference) {
        sendMessage(new OfflineQaUserCommand(caseUUID, stageUUID, caseReference, offlineUserUUID, currentUserUUID),
            LogEvent.OFFLINE_QA_EMAIL_SENT);
    }

    private void sendMessage(NotifyCommand command, LogEvent event) {
        try {
            var messageRequest = SendMessageRequest.builder()
                .queueUrl(notifyQueueUrl)
                .messageAttributes(getQueueHeaders(event.toString()))
                .messageBody(objectMapper.writeValueAsString(command)).build();

            notifyAsyncClient.sendMessage(messageRequest);

            log.info("Sent email message of type {}", command.getCommand(), value(LogEvent.EVENT, event));
        } catch (JsonProcessingException e) {
            log.error("Failed to send email message of type {}", command.getCommand(),
                value(LogEvent.EVENT, LogEvent.NOTIFY_EMAIL_FAILED), value(LogEvent.EXCEPTION, e));
        }
    }

    private Map<String, MessageAttributeValue> getQueueHeaders(String eventType) {

        return Map.of(EVENT_TYPE_HEADER,
            MessageAttributeValue.builder()
                .dataType(RequestData.CORRELATION_ID_HEADER).stringValue(requestData.correlationId())
                .dataType(RequestData.CORRELATION_ID_HEADER).stringValue(requestData.userId())
                .dataType(RequestData.CORRELATION_ID_HEADER).stringValue(requestData.username())
                .dataType(RequestData.CORRELATION_ID_HEADER).stringValue(requestData.groups())
                .build());

        /*return Map.of(EVENT_TYPE_HEADER, new MessageAttributeValue(eventType),
            RequestData.CORRELATION_ID_HEADER, new SqsStringMessageAttributeValue(requestData.correlationId()),
            RequestData.USER_ID_HEADER, new SqsStringMessageAttributeValue(requestData.userId()),
            RequestData.USERNAME_HEADER, new SqsStringMessageAttributeValue(requestData.username()),
            RequestData.GROUP_HEADER, new SqsStringMessageAttributeValue(requestData.groups()));*/
    }
}
