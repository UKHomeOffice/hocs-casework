package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
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
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.OfflineQaUserCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.TeamAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.UserAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.util.SqsStringMessageAttributeValue;
import uk.gov.digital.ho.hocs.casework.utils.BaseAwsTest;

import java.util.Map;
import java.util.UUID;

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
    private AmazonSQSAsync notifySqsClient;

    @MockBean(name = "requestData")
    private RequestData requestData;

    private ResultCaptor<SendMessageResult> sqsMessageResult;

    @Autowired
    private NotifyClient notifyClient;

    @Before
    public void setup() {
        when(requestData.correlationId()).thenReturn(randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        sqsMessageResult = new ResultCaptor<>();
        doAnswer(sqsMessageResult).when(notifySqsClient).sendMessage(any());
    }

    @Test
    public void shouldSendUserAssignedCommand() {
        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        var userAssignedCommand = new UserAssignChangeCommand(caseUUID, stageUUID, caseRef, currentUser, newUser);
        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);

        assertSqsValue(userAssignedCommand);
    }

    @Test
    public void shouldSendTeamAssignedCommand() {
        UUID teamUuid = UUID.randomUUID();

        var teamAssignChangeCommand = new TeamAssignChangeCommand(caseUUID, stageUUID, caseRef, teamUuid, "TEST");
        notifyClient.sendTeamEmail(caseUUID, stageUUID, teamUuid, caseRef, "TEST");

        assertSqsValue(teamAssignChangeCommand);
    }

    @Test
    public void shouldSendOfflineQAUserCommand() {
        UUID currentUser = UUID.randomUUID();
        UUID offlineUser = UUID.randomUUID();

        var offlineQaUserCommand = new OfflineQaUserCommand(caseUUID, stageUUID, caseRef, offlineUser, currentUser);
        notifyClient.sendOfflineQaEmail(caseUUID, stageUUID, currentUser, offlineUser, caseRef);

        assertSqsValue(offlineQaUserCommand);
    }

    @Test
    public void shouldSetHeaders() {
        Map<String, MessageAttributeValue> expectedHeaders = Map.of("event_type",
            new SqsStringMessageAttributeValue(LogEvent.USER_EMAIL_SENT.toString()), RequestData.CORRELATION_ID_HEADER,
            new SqsStringMessageAttributeValue(requestData.correlationId()), RequestData.USER_ID_HEADER,
            new SqsStringMessageAttributeValue(requestData.userId()), RequestData.USERNAME_HEADER,
            new SqsStringMessageAttributeValue(requestData.username()), RequestData.GROUP_HEADER,
            new SqsStringMessageAttributeValue(requestData.groups()));

        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);

        verify(notifySqsClient).sendMessage(messageCaptor.capture());
        Assertions.assertEquals(messageCaptor.getValue().getMessageAttributes(), expectedHeaders);
    }

    private void assertSqsValue(Object command) {
        Assertions.assertNotNull(sqsMessageResult);

        // getMessageMd5 - toString strips leading zeros, 31/32 matched is close enough in this instance
        Assertions.assertTrue(sqsMessageResult.getResult().getMD5OfMessageBody().contains(getMessageMd5(command)));
    }

}
