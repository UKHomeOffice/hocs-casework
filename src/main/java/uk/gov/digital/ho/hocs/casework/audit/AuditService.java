package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;


@Service
@Slf4j
public class AuditService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void createAuditEntry(UUID uuid, AuditAction action, String username, String caseData){

            AuditEntry auditEntry = auditRepository.findByUUID(uuid);
            if(auditEntry == null){
                try {
                auditRepository.save(new AuditEntry(uuid, action, username, caseData));
                } catch (DataIntegrityViolationException e) {
                    if (e.getCause() instanceof ConstraintViolationException &&
                            (((ConstraintViolationException) e.getCause()).getConstraintName().toLowerCase().contains("audit_id_idempotent"))) {
                        // Do Nothing.
                        log.info("Received duplicate message {}, {}", auditEntry.getEventUUID(), auditEntry.getEventTimestamp());
                    } else {
                        throw e;
                    }
                }
            }
            else


            log.error("Failed to write audit line {}", caseData);

    }
}

