package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotifyClientTest {

    @Mock
    private RequestData requestData;

    @Mock
    private InfoClient infoClient;

    private NotifyClient notifyClient;

    @Before
    public void setup() {
        notifyClient = new NotifyClient(infoClient, "12345", "", requestData);
    }

    private UUID caseUUID = UUID.randomUUID();
    private UUID stageUUID = UUID.randomUUID();

    private String caseRef = "";

    @Test
    public void ShouldNotSendSelfEmailUnAllocated() {

        UUID currentUserUUID = null;
        UUID newUserUUID = UUID.fromString("11111111-0000-0000-0000-000000000000");

        when(requestData.userIdUUID()).thenReturn(UUID.fromString("11111111-0000-0000-0000-000000000000"));

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseRef);

        verify(requestData, times(1)).userIdUUID();

        // No email means we don't look the user up to get their email address.
        verifyZeroInteractions(infoClient);
        verifyNoMoreInteractions(requestData);
    }

    @Test
    public void ShouldSendOtherEmailUnAllocated() {

        UUID currentUserUUID = null;
        UUID newUserUUID = UUID.fromString("11111111-0000-0000-0000-000000000000");

        when(infoClient.getUser(newUserUUID)).thenReturn(new UserDto("any", "any", "any", "any"));
        when(requestData.userIdUUID()).thenReturn(UUID.fromString("22222222-0000-0000-0000-000000000000"));

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseRef);

        verify(requestData, times(1)).userIdUUID();
        verify(infoClient, times(1)).getUser(newUserUUID);

        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(requestData);
    }

    @Test
    public void ShouldNotSendSelfEmailAllocated() {

        UUID currentUserUUID = UUID.fromString("33333333-0000-0000-0000-000000000000");
        UUID newUserUUID = UUID.fromString("11111111-0000-0000-0000-000000000000");

        when(infoClient.getUser(currentUserUUID)).thenReturn(new UserDto("any", "any", "any", "any"));
        when(requestData.userIdUUID()).thenReturn(UUID.fromString("11111111-0000-0000-0000-000000000000"));

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseRef);

        verify(requestData, times(1)).userIdUUID();
        verify(infoClient, times(1)).getUser(currentUserUUID);

        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(requestData);
    }

    @Test
    public void ShouldSendOtherEmailAllocated() {

        UUID currentUserUUID = UUID.fromString("33333333-0000-0000-0000-000000000000");
        UUID newUserUUID = UUID.fromString("11111111-0000-0000-0000-000000000000");

        when(infoClient.getUser(currentUserUUID)).thenReturn(new UserDto("any", "any", "any", "any"));
        when(infoClient.getUser(newUserUUID)).thenReturn(new UserDto("any", "any", "any", "any"));
        when(requestData.userIdUUID()).thenReturn(UUID.fromString("22222222-0000-0000-0000-000000000000"));

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseRef);

        verify(requestData, times(1)).userIdUUID();
        verify(infoClient, times(1)).getUser(newUserUUID);
        verify(infoClient, times(1)).getUser(currentUserUUID);


        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(requestData);
    }


    @Test
    public void ShouldAlwaysSendEmailUnAllocate() {

        UUID currentUserUUID = UUID.fromString("33333333-0000-0000-0000-000000000000");
        UUID newUserUUID = null;

        when(infoClient.getUser(currentUserUUID)).thenReturn(new UserDto("any", "any", "any", "any"));

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseRef);

        verify(infoClient, times(1)).getUser(currentUserUUID);

        verifyNoMoreInteractions(infoClient);
        verifyZeroInteractions(requestData);
    }

}