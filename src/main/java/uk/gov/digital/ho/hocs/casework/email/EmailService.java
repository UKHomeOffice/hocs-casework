package uk.gov.digital.ho.hocs.casework.email;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

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

    public void sendRshEmail(SendEmailRequest notifyRequest, UUID caseUUID, String reference, String caseStatus, String userName) {
        sendNotify(notifyRequest, caseUUID, rshTemplateId, reference, caseStatus, userName);
    }

    private void sendNotify(SendEmailRequest emailRequest, UUID caseUUID, String templateId, String reference, String caseStatus, String username) {
        if (emailRequest != null) {
            auditService.writeSendEmailEvent(username, emailRequest);
            if (!isNullOrEmpty(emailRequest.getEmail()) && !isNullOrEmpty(emailRequest.getTeamName())) {
                log.info("Received request to email {} {}, templateId {}", emailRequest.getEmail(), emailRequest.getTeamName(), templateId);
                notifyClient.sendEmail(emailRequest.getEmail(), emailRequest.getTeamName(), frontEndUrl, caseUUID, reference, caseStatus, templateId);
            } else {
                log.warn("Received request to email templateId {}, but params were null!", templateId);
            }
        } else {
            log.warn("Received request to email, but request was null!");
        }
    }


}