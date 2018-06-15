package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;

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
    private final String emailAddress = "email.user@test.com";
    private final String team = "Test Team";
    private final String testUser = "test user";

    @Before
    public void setUp() {
        this.emailService = new EmailService(
                mockNotifyClient, templateId, frontendUrl, mockAuditService);
    }

    @Test
    public void shouldSendRshNotify() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        doNothing().when(mockNotifyClient).sendEmail(any(Email.class), anyString());

        emailService.sendRshEmail(new SendRshEmailRequest(emailAddress, team), uuid, reference, "CaseStatus", testUser);

        verify(mockNotifyClient, times(1)).sendEmail(any(Email.class), eq(frontendUrl));
    }

    @Test
    public void shouldSendRshNotifyNullEmail() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        emailService.sendRshEmail(new SendRshEmailRequest(null, team), uuid, reference, "CaseStatus", testUser);

        verify(mockNotifyClient, times(0)).sendEmail(any(Email.class), eq(frontendUrl));
    }

    @Test
    public void shouldSendRshNotifyNullTeamName() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);
        Email email = new Email("", "", UUID.randomUUID(), "", "", "");

        emailService.sendRshEmail(new SendRshEmailRequest(emailAddress, null), uuid, reference, "CaseStatus", testUser);

        verify(mockNotifyClient, times(0)).sendEmail(any(Email.class), eq(frontendUrl));
    }

    @Test
    public void shouldSendRshNotifyNullNotifyRequest() {
        String reference = String.format("%s/case/%s", frontendUrl, uuid);

        emailService.sendRshEmail(null, uuid, reference, "CaseStatus", testUser);

        verify(mockNotifyClient, times(0)).sendEmail(any(Email.class), eq(frontendUrl));
    }

}