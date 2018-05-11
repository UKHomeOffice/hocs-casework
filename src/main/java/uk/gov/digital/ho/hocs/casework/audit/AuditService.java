package uk.gov.digital.ho.hocs.casework.audit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
@Slf4j
public class AuditService {

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void createAuditEntry(String uuid, String action, String username, String caseData) {
        this.auditRepository.save(new AuditEntry(uuid, LocalDateTime.now().toString(), action, username, caseData));
    }
}

