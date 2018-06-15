package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.model.Email;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;


@Service
@Slf4j
public class EmailService {

    private final AuditService auditService;

    private final NotifyClient notifyClient;

    private final String rshTemplateId;
    private final String frontEndUrl;

    @Autowired
    public EmailService(NotifyClient notifyClient,
                        @Value("${notify.rshTemplateId}") String rshTemplateId,
                        @Value("${notify.frontend.url}") String frontEndUrl,
                        AuditService auditService) {
        this.notifyClient = notifyClient;
        this.rshTemplateId = rshTemplateId;
        this.frontEndUrl = frontEndUrl;
        this.auditService = auditService;
    }

    public void sendRshEmail(SendRshEmailRequest notifyRequest, UUID caseUUID, String caseReference, String caseStatus, String userName) {
        if (notifyRequest != null) {
            Email email = new Email(notifyRequest.getEmail(), notifyRequest.getTeamName(), caseUUID, caseReference, caseStatus, rshTemplateId);
            sendEmail(email, userName);
        } else {
            log.warn("Received request to email, but notify request was null!");
        }
    }

    private void sendEmail(Email email, String username) {
        if (email != null) {
            if (!isNullOrEmpty(email.getEmailAddress()) && !isNullOrEmpty(email.getTeamName())) {
                log.info("Received request to email {} {}, templateId {}", email.getEmailAddress(), email.getTeamName(), email.getTemplateId());
                auditService.writeSendEmailEvent(username, email);
                notifyClient.sendEmail(email, frontEndUrl);
            } else {
                log.warn("Received request to email templateId {}, but params were null!", email.getTemplateId());
            }
        } else {
            log.warn("Received request to email, but request was null!");
        }
    }
}