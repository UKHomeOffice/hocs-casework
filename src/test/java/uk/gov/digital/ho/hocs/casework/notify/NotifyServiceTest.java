package uk.gov.digital.ho.hocs.casework.notify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.model.NotifyRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;
import uk.gov.service.notify.SendEmailResponse;

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

    private final String templateId = "TEMPLATE_ID";
    private final String frontendUrl = "FRONTEND_URL";

    private final UUID uuid = UUID.randomUUID();
    private final String email = "email.user@test.com";
    private final String team = "Test Team";

    @Before
    public void setUp() throws NotificationClientException {
        this.notifyService = new NotifyService(
                notifyClient, templateId, frontendUrl
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
        notifyService.sendRshNotify(new NotifyRequest(email, team), uuid);

        final Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", team);
        personalisation.put("link", String.format("%s/case/%s", frontendUrl, uuid));

        verify(mockNotify).sendEmail(
                templateId,
                email,
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
        notifyService.sendNotify(new NotifyRequest(email, team), uuid, templateId);

        final Map<String, String> personalisation = new HashMap<>();
        personalisation.put("team", team);
        personalisation.put("link", String.format("%s/case/%s", frontendUrl, uuid));

        verify(mockNotify).sendEmail(
                templateId,
                email,
                personalisation,
                null,
                null
        );

    }

}
