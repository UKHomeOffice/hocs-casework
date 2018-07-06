package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.notifyClient.ProxyingNotificationClient;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class EmailService {

    private final AuditService auditService;

    private final ProxyingNotificationClient notifyClient;

    private final String rshTemplate;


    @Autowired
    public EmailService(ProxyingNotificationClient notifyClient,
                        AuditService auditService,
                        @Value("${notify.rshTemplateId}") String rshTemplateId) {
        this.notifyClient = notifyClient;
        this.auditService = auditService;
        this.rshTemplate = rshTemplateId;
    }

    public void sendRshEmail(SendEmailRequest sendEmailRequest) {
        sendEmail(sendEmailRequest, rshTemplate);
    }

    private void sendEmail(SendEmailRequest sendEmailRequest, String templateId) {
        if (sendEmailRequest != null) {
            if (!isNullOrEmpty(sendEmailRequest.getEmailAddress())) {
                log.info("Received request to sendEmailRequest {}, templateId {}", sendEmailRequest.getEmailAddress(), templateId);
                auditService.writeSendEmailEvent(sendEmailRequest);
                notifyClient.sendEmail(sendEmailRequest.getEmailAddress(), sendEmailRequest.getPersonalisation(), templateId);
            } else {
                log.warn("Received request to sendEmailRequest templateId {}, but params were null!", templateId);
            }
        } else {
            log.warn("Received request to sendEmailRequest, but request was null!");
        }
    }
}