package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.HashMap;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SendEmailRequestServiceTest {

    private EmailService emailService;

    @Mock
    private AuditService mockAuditService;
    @Mock
    private ProxyingNotificationClient mockNotifyClient;


    @Before
    public void setUp() {
        this.emailService = new EmailService(
                mockNotifyClient, mockAuditService, "");
    }

    @Test
    public void shouldSendRshEmail() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest("email.user@test.com", new HashMap<>());
        emailService.sendRshEmail(sendEmailRequest);

        verify(mockNotifyClient, times(1)).sendEmail(any(SendEmailRequest.class), anyString());
    }

    @Test
    public void shouldNotSendEmailNullEmailAddress() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest(null, new HashMap<>());
        emailService.sendRshEmail(sendEmailRequest);

        verify(mockNotifyClient, times(0)).sendEmail(any(SendEmailRequest.class), anyString());
    }

    @Test
    public void shouldSendEmailNullTeamName() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest("email.user@test.com", new HashMap<>());
        emailService.sendRshEmail(sendEmailRequest);

        verify(mockNotifyClient, times(1)).sendEmail(any(SendEmailRequest.class), anyString());
    }

    @Test
    public void shouldSendEmailNullPersonalisation() {
        SendEmailRequest sendEmailRequest = new SendEmailRequest("email.user@test.com", null);
        emailService.sendRshEmail(sendEmailRequest);

        verify(mockNotifyClient, times(1)).sendEmail(any(SendEmailRequest.class), anyString());
    }

    @Test
    public void shouldNotSendEmailNullNotifyRequest() {
        emailService.sendRshEmail(null);

        verify(mockNotifyClient, times(0)).sendEmail(any(SendEmailRequest.class), anyString());
    }


}