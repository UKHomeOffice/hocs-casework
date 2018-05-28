package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.model.CorrelationDetails;
import uk.gov.digital.ho.hocs.casework.model.RshCaseCreateRequest;
import uk.gov.digital.ho.hocs.casework.rsh.CaseDetails;
import uk.gov.digital.ho.hocs.casework.rsh.StageDetails;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void createAuditEntry(CaseDetails caseDetails, AuditAction auditAction, CorrelationDetails correlationDetails, String request){
        AuditEntry auditEntry = new AuditEntry(correlationDetails, caseDetails.getUuid(), "CREATE", auditAction, request);
        createAuditEntry(auditEntry);
    }

    public void createAuditEntry(StageDetails stageDetails, AuditAction auditAction, CorrelationDetails correlationDetails, String request) {
        AuditEntry auditEntry = new AuditEntry(correlationDetails, stageDetails.getCaseUUID(), stageDetails.getName(), auditAction, request);
        createAuditEntry(auditEntry);
    }

    private void createAuditEntry(AuditEntry auditEntry) {
        try {
        auditRepository.save(auditEntry);
        log.info("Saved message {}, {}", auditEntry.getEventUUID(), auditEntry.getEventTimestamp());
        } catch (DataIntegrityViolationException e) {
            if (e.getCause() instanceof ConstraintViolationException &&
                    (((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("audit_id_idempotent"))) {
                // Do Nothing.
                log.info("Duplicate message {}, {}", auditEntry.getEventUUID(), auditEntry.getEventTimestamp());
            } else {
                throw e;
            }
        }
    }
}

