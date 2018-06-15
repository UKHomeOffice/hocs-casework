package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;


@Service
@Slf4j
public class EmailService {

    private final AuditService auditService;

    private final ProxyingNotificationClient notifyClient;

    @Autowired
    public EmailService(ProxyingNotificationClient notifyClient,
                        AuditService auditService) {
        this.notifyClient = notifyClient;
        this.auditService = auditService;
    }

    public void sendEmail(Email email, String username) {
        if (email != null) {
            if (!isNullOrEmpty(email.getEmailAddress())) {
                log.info("Received request to email {}, templateId {}", email.getEmailAddress(), email.getTemplateId());
                auditService.writeSendEmailEvent(username, email);
                notifyClient.sendEmail(email);
            } else {
                log.warn("Received request to email templateId {}, but params were null!", email.getTemplateId());
            }
        } else {
            log.warn("Received request to email, but request was null!");
        }
    }
}