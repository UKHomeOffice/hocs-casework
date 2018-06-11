package uk.gov.digital.ho.hocs.casework.notify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.model.NotifyRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class NotifyServiceTest {

    private NotifyService notifyService;

    @Mock
    private NotifyClient notifyClient;
    @Mock
    private NotificationClient mockNotify;

    private final String TEMPLATE_ID = "TEMPLATE_ID";
    private final String FRONTEND_URL = "FRONTEND_URL";

    private final UUID LINK_UUID = UUID.randomUUID();
    private final String EMIAL = "EMIAL.user@test.com";
    private final String TEAM = "Test Team";
    private final String REFERENCE = "AAA/00/18";
    private final String CASE_STATUS = "pending Callback";

    @Before
    public void setUp() throws NotificationClientException {
        this.notifyService = new NotifyService(
                notifyClient, TEMPLATE_ID, FRONTEND_URL
        );
    }

    @Test
    public void shouldSendRshNotify() throws NotificationClientException {
        when(notifyClient.getClient()).thenReturn(mockNotify);
        when(mockNotify.sendEmail(
                anyString(),
                anyString(),
                anyMap(),
                any(),
                any()
        )).thenReturn(null);
        notifyService.sendRshNotify(new NotifyRequest(EMIAL, TEAM),LINK_UUID, REFERENCE, CASE_STATUS);

        final Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", TEAM);
        personalisation.put("link", String.format("%s/case/%s", FRONTEND_URL, LINK_UUID));
        personalisation.put("reference", REFERENCE);
        personalisation.put("caseStatus", CASE_STATUS);

        verify(mockNotify).sendEmail(
                TEMPLATE_ID,
                EMIAL,
                personalisation,
                null,
                null
        );

    }

    @Test
    public void shouldSendEmail() throws NotificationClientException {
        when(notifyClient.getClient()).thenReturn(mockNotify);
        when(mockNotify.sendEmail(
                anyString(),
                anyString(),
                anyMap(),
                any(),
                any()
        )).thenReturn(null);
        notifyService.sendNotify(new NotifyRequest(EMIAL, TEAM), LINK_UUID, REFERENCE, CASE_STATUS, TEMPLATE_ID);

        final Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", TEAM);
        personalisation.put("link", String.format("%s/case/%s", FRONTEND_URL, LINK_UUID));
        personalisation.put("reference", REFERENCE);
        personalisation.put("caseStatus", CASE_STATUS);

        verify(mockNotify).sendEmail(
                TEMPLATE_ID,
                EMIAL,
                personalisation,
                null,
                null
        );

    }

}
