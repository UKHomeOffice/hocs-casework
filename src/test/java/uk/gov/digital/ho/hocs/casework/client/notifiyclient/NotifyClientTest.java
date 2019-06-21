package uk.gov.digital.ho.hocs.casework.client.notifiyclient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.SpringConfiguration;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.TeamAssignChangeCommand;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.dto.UserAssignChangeCommand;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotifyClientTest {

    @Mock
    private RequestData requestData;

    @Mock
    ProducerTemplate producerTemplate;

    private SpringConfiguration configuration = new SpringConfiguration();
    private ObjectMapper mapper;
    private String notifyQueue ="notify-queue";
    private UUID caseUUID = UUID.randomUUID();
    private UUID stageUUID = UUID.randomUUID();

    private String caseRef = "";

    @Captor
    ArgumentCaptor jsonCaptor;

    @Captor
    ArgumentCaptor<HashMap<String,Object>> headerCaptor;

    private NotifyClient notifyClient;

    @Before
    public void setup() {
        when(requestData.correlationId()).thenReturn(randomUUID().toString());
        when(requestData.userId()).thenReturn("some user id");
        when(requestData.groups()).thenReturn("some groups");
        when(requestData.username()).thenReturn("some username");

        mapper = configuration.initialiseObjectMapper();
        notifyClient = new NotifyClient(producerTemplate, notifyQueue, mapper, requestData);
    }

    @Test
    public void shouldSetUserDataFields() throws IOException {
        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);

        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(notifyQueue), jsonCaptor.capture(), any());
        UserAssignChangeCommand request = mapper.readValue((String)jsonCaptor.getValue(), UserAssignChangeCommand.class);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(request.getStageUUID()).isEqualTo(stageUUID);
        assertThat(request.getCurrentUserUUID()).isEqualTo(currentUser);
        assertThat(request.getNewUserUUID()).isEqualTo(newUser);
        assertThat(request.getCaseReference()).isEqualTo(caseRef);

    }

    @Test
    public void shouldSetTeamDataFields() throws IOException {
        UUID teamUUID = UUID.randomUUID();

        notifyClient.sendTeamEmail(caseUUID, stageUUID, teamUUID, caseRef, "something");

        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(notifyQueue), jsonCaptor.capture(), any());
        TeamAssignChangeCommand request = mapper.readValue((String)jsonCaptor.getValue(), TeamAssignChangeCommand.class);
        assertThat(request.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(request.getStageUUID()).isEqualTo(stageUUID);
        assertThat(request.getTeamUUID()).isEqualTo(teamUUID);
        assertThat(request.getAllocationType()).isEqualTo("something");
        assertThat(request.getCaseReference()).isEqualTo(caseRef);

    }

    @Test
    public void shouldSetHeaders()  {
        Map<String, Object> expectedHeaders = Map.of(
                RequestData.CORRELATION_ID_HEADER, requestData.correlationId(),
                RequestData.USER_ID_HEADER, requestData.userId(),
                RequestData.USERNAME_HEADER, requestData.username(),
                RequestData.GROUP_HEADER, requestData.groups());

        UUID currentUser = UUID.randomUUID();
        UUID newUser = UUID.randomUUID();

        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUser, newUser, caseRef);
        verify(producerTemplate, times(1)).sendBodyAndHeaders(eq(notifyQueue), any(), headerCaptor.capture());
        Map headers = headerCaptor.getValue();

        assertThat(headers).containsAllEntriesOf(expectedHeaders);
    }

}