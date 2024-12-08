package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.OfflineQaUserCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.TeamAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.UserAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.utils.BaseAwsTest;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("local")
public class NotifyClientTest extends BaseAwsTest {

    private static final UUID caseUUID = UUID.randomUUID();

    private static final UUID stageUUID = UUID.randomUUID();

    private static final String caseRef = "";

    @Captor
    ArgumentCaptor<SendMessageRequest> messageCaptor;

    @SpyBean
    private SqsAsyncClient notifySqsClient;

    @MockBean(name = "requestData")
    private RequestData requestData;

    private ResultCaptor<CompletableFuture<SendMessageResponse>> sqsMessageResult;

    @Autowired
    private NotifyClient notifyClient;

    @Before
    public void setup() {
        when(requestData.correlationId()).thenReturn(randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        sqsMessageResult = new ResultCaptor<>();
        doAnswer(sqsMessageResult).when(notifySqsClient).sendMessage((SendMessageRequest) any());
    }

    @Test
    public void shouldSendUserAssignedCommand() throws ExecutionException, InterruptedException {
        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        var userAssignedCommand = new UserAssignChangeCommand(caseUUID, stageUUID, caseRef, currentUser, newUser);
        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);

        assertSqsValue(userAssignedCommand);
    }

    @Test
    public void shouldSendTeamAssignedCommand() throws ExecutionException, InterruptedException {
        UUID teamUuid = UUID.randomUUID();

        var teamAssignChangeCommand = new TeamAssignChangeCommand(caseUUID, stageUUID, caseRef, teamUuid, "TEST");
        notifyClient.sendTeamEmail(caseUUID, stageUUID, teamUuid, caseRef, "TEST");

        assertSqsValue(teamAssignChangeCommand);
    }

    @Test
    public void shouldSendOfflineQAUserCommand() throws ExecutionException, InterruptedException {
        UUID currentUser = UUID.randomUUID();
        UUID offlineUser = UUID.randomUUID();

        var offlineQaUserCommand = new OfflineQaUserCommand(caseUUID, stageUUID, caseRef, offlineUser, currentUser);
        notifyClient.sendOfflineQaEmail(caseUUID, stageUUID, currentUser, offlineUser, caseRef);

        assertSqsValue(offlineQaUserCommand);
    }

    @Test
    public void shouldSetHeaders() {
        Map<String, MessageAttributeValue> expectedHeaders = Map.of(
            "event_type", MessageAttributeValue.builder().dataType("String").stringValue(LogEvent.USER_EMAIL_SENT.toString()).build(),
            RequestData.CORRELATION_ID_HEADER, MessageAttributeValue.builder().dataType("String").stringValue(requestData.correlationId()).build(),
            RequestData.USER_ID_HEADER, MessageAttributeValue.builder().dataType("String").stringValue(requestData.userId()).build(),
            RequestData.USERNAME_HEADER, MessageAttributeValue.builder().dataType("String").stringValue(requestData.username()).build(),
            RequestData.GROUP_HEADER, MessageAttributeValue.builder().dataType("String").stringValue(requestData.groups()).build());

        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);

        verify(notifySqsClient).sendMessage(messageCaptor.capture());
        Assertions.assertEquals(messageCaptor.getValue().messageAttributes(), expectedHeaders);

    }

    private void assertSqsValue(Object command) throws ExecutionException, InterruptedException {
        Assertions.assertNotNull(sqsMessageResult);

        // getMessageMd5 - toString strips leading zeros, 31/32 matched is close enough in this instance
        Assertions.assertTrue(sqsMessageResult.getResult().get().md5OfMessageBody().contains(getMessageMd5(command)));

    }

}
