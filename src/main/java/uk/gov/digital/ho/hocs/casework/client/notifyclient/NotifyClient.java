package uk.gov.digital.ho.hocs.casework.client.notifyclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.NotifyCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.OfflineQaUserCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.TeamAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.UserAssignChangeCommand;

import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Component
public class NotifyClient {

    private final String notifyQueue;
    private final ProducerTemplate producerTemplate;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;


    @Autowired
    public NotifyClient(ProducerTemplate producerTemplate,
                        @Value("${notify.queue}") String notifyQueue,
                        ObjectMapper objectMapper,
                        RequestData requestData) {
        this.producerTemplate = producerTemplate;
        this.notifyQueue = notifyQueue;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    @Async
    public void sendTeamEmail(UUID caseUUID, UUID stageUUID, UUID teamUUID, String caseReference, String emailType)  {
        sendTeamEmailCommand(new TeamAssignChangeCommand(caseUUID, stageUUID, caseReference, teamUUID, emailType));
    }

    @Async
    public void sendUserEmail(UUID caseUUID, UUID stageUUID, UUID currentUserUUID, UUID newUserUUID, String caseReference)  {
        sendUserEmailCommand(new UserAssignChangeCommand(caseUUID, stageUUID, caseReference, currentUserUUID, newUserUUID));
    }

    @Async
    public void sendOfflineQaEmail(UUID caseUUID, UUID stageUUID, UUID currentUserUUID, UUID offlineUserUUID, String caseReference)  {
        sendOfflineQaUserCommand(new OfflineQaUserCommand(caseUUID, stageUUID, caseReference, offlineUserUUID, currentUserUUID));
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    private void sendTeamEmailCommand(TeamAssignChangeCommand command){
        try {
            Map<String, Object> queueHeaders = getQueueHeaders();
            producerTemplate.sendBodyAndHeaders(notifyQueue, objectMapper.writeValueAsString(command), queueHeaders);
            log.info("Sent Team Email of type {} for Case UUID: {}, correlationID: {}, UserID: {}", command.getCommand(), command.getCaseUUID(), requestData.correlationId(), requestData.userId(), value(EVENT, TEAM_EMAIL_SENT));
        } catch (Exception e) {
            logFailedToSendEmail(command, e);
        }
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    private void sendUserEmailCommand(UserAssignChangeCommand command){
        try {
            Map<String, Object> queueHeaders = getQueueHeaders();
            producerTemplate.sendBodyAndHeaders(notifyQueue, objectMapper.writeValueAsString(command), queueHeaders);
            log.info("Sent User Email of type {} for Case UUID: {}, correlationID: {}, UserID: {}", command.getCommand(), command.getCaseUUID(), requestData.correlationId(), requestData.userId(), value(EVENT, TEAM_EMAIL_SENT));
        } catch (Exception e) {
            logFailedToSendEmail(command, e);
        }
    }

    @Retryable(maxAttemptsExpression = "${retry.maxAttempts}", backoff = @Backoff(delayExpression = "${retry.delay}"))
    private void sendOfflineQaUserCommand(OfflineQaUserCommand command){
        try {
            Map<String, Object> queueHeaders = getQueueHeaders();
            producerTemplate.sendBodyAndHeaders(notifyQueue, objectMapper.writeValueAsString(command), queueHeaders);
            log.info("Sent Offline QA Email of type {} for Case UUID: {}, correlationID: {}, UserID: {}", command.getCommand(), command.getCaseUUID(), requestData.correlationId(), requestData.userId(), value(EVENT, OFFLINE_QA_EMAIL_SENT));
        } catch (Exception e) {
            logFailedToSendEmail(command, e);
        }
    }

    private void logFailedToSendEmail(NotifyCommand command, Exception e){
        log.error("Failed to send Email for case UUID {}", command.getCaseUUID(), value(EVENT, NOTIFY_EMAIL_FAILED), value(EXCEPTION, e));
    }

    private Map<String, Object> getQueueHeaders() {
        return Map.of(
        RequestData.CORRELATION_ID_HEADER, requestData.correlationId(),
        RequestData.USER_ID_HEADER, requestData.userId(),
        RequestData.USERNAME_HEADER, requestData.username(),
        RequestData.GROUP_HEADER, requestData.groups());
    }
}
