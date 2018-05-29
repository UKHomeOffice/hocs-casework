package uk.gov.digital.ho.hocs.casework.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.caseDetails.StageDetails;

import java.util.UUID;


@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void createAuditEntry(AuditAction auditAction, String username, String request){
        AuditEntry auditEntry = new AuditEntry(username, null, "CREATE", auditAction, request);
        createAuditEntry(auditEntry);
    }

    public void createAuditEntry(UUID caseUUID, AuditAction auditAction, String username, String request){
        AuditEntry auditEntry = new AuditEntry(username, caseUUID, "CREATE", auditAction, request);
        createAuditEntry(auditEntry);
    }

    public void createAuditEntry(StageDetails stageDetails, AuditAction auditAction, String username, String request) {
        AuditEntry auditEntry = new AuditEntry(username, stageDetails.getCaseUUID(), stageDetails.getName(), auditAction, request);
        createAuditEntry(auditEntry);
    }

    private void createAuditEntry(AuditEntry auditEntry) {
        auditRepository.save(auditEntry);
        log.info("Saved message {}, {}, {}", auditEntry.getUsername(), auditEntry.getEventAction(), auditEntry.getCaseUUID());
    }
}

