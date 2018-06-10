package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.service.notify.NotificationClient;
import uk.gov.service.notify.NotificationClientException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    private EmailService emailService;

    @Mock
    private AuditService auditService;
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
    public void setUp() {
        this.emailService = new EmailService(
                notifyClient, templateId, frontendUrl, auditService);
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
        emailService.sendRshNotify(new SendEmailRequest(email, team), uuid, email);

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