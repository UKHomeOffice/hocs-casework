package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    private EmailService emailService;

    @Mock
    private AuditService mockAuditService;
    @Mock
    private NotifyClient mockNotifyClient;

    private final String templateId = "TEMPLATE_ID";
    private final String frontendUrl = "FRONTEND_URL";

    private final UUID uuid = UUID.randomUUID();
    private final String email = "email.user@test.com";
    private final String team = "Test Team";

    @Before
    public void setUp() {
        this.emailService = new EmailService(
                mockNotifyClient, templateId, frontendUrl, mockAuditService);
    }

    @Test
    public void shouldSendRshNotify() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        doNothing().when(mockNotifyClient).sendEmail(
                anyString(),
                anyString(),
                anyString(),
                any(UUID.class),
                anyString(),
                anyString(),
                anyString()
        );

        emailService.sendRshEmail(new SendEmailRequest(email, team), uuid, reference, "CaseStatus", email);

        verify(mockNotifyClient, times(1)).sendEmail(
                email,
                team,
                frontendUrl,
                uuid,
                reference,
                "CaseStatus",
                templateId
        );
    }

    @Test
    public void shouldSendRshNotifyNullEmail() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        emailService.sendRshEmail(new SendEmailRequest(null, team), uuid, reference, "CaseStatus", email);

        verify(mockNotifyClient, times(0)).sendEmail(
                email,
                team,
                frontendUrl,
                uuid,
                reference,
                "CaseStatus",
                templateId
        );
    }

    @Test
    public void shouldSendRshNotifyNullNotify() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        emailService.sendRshEmail(new SendEmailRequest(email, null), uuid, reference, "CaseStatus", email);

        verify(mockNotifyClient, times(0)).sendEmail(
                email,
                team,
                frontendUrl,
                uuid,
                reference,
                "CaseStatus",
                templateId
        );
    }

    @Test
    public void shouldSendRshNotifyNullNotifyRequest() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        emailService.sendRshEmail(null, uuid, reference, "CaseStatus", email);

        verify(mockNotifyClient, times(0)).sendEmail(
                email,
                team,
                frontendUrl,
                uuid,
                reference,
                "CaseStatus",
                templateId
        );
    }

}