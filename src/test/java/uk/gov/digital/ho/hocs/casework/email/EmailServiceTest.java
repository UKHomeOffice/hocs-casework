package uk.gov.digital.ho.hocs.casework.email;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;

import java.util.HashMap;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailServiceTest {

    private EmailService emailService;

    @Mock
    private AuditService mockAuditService;
    @Mock
    private ProxyingNotificationClient mockNotifyClient;

    private final String templateId = "TEMPLATE_ID";

    private final String emailAddress = "email.user@test.com";
    private final String testUser = "test user";


    @Before
    public void setUp() {
        this.emailService = new EmailService(
                mockNotifyClient, mockAuditService);
    }

    @Test
    public void shouldSendEmail() {
        doNothing().when(mockNotifyClient).sendEmail(any(Email.class));

        Email email = new Email(emailAddress, templateId, new HashMap<>());
        emailService.sendEmail(email, testUser);

        verify(mockNotifyClient, times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void shouldSendEmailNullEmailAddress() {
        Email email = new Email(null, templateId, new HashMap<>());
        emailService.sendEmail(email, testUser);

        verify(mockNotifyClient, times(0)).sendEmail(any(Email.class));
    }

    @Test
    public void shouldSendEmailNullTeamName() {
        Email email = new Email(emailAddress, null, new HashMap<>());
        emailService.sendEmail(email, testUser);

        verify(mockNotifyClient, times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void shouldSendEmailNullPersonalisation() {
        Email email = new Email(emailAddress, templateId, null);
        emailService.sendEmail(email, testUser);

        verify(mockNotifyClient, times(1)).sendEmail(any(Email.class));
    }

    @Test
    public void shouldSendEmailNullNotifyRequest() {
        emailService.sendEmail(null, testUser);

        verify(mockNotifyClient, times(0)).sendEmail(any(Email.class));
    }


}