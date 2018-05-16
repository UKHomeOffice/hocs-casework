package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;


@Service
@Slf4j
public class AuditService {

    private ObjectMapper objectMapper = new ObjectMapper();

    private final AuditRepository auditRepository;

    @Autowired
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void createAuditEntry(String uuid, String action, String username, Map<String,Object> caseData) throws JsonProcessingException {
        this.auditRepository.save(new AuditEntry(uuid, LocalDateTime.now(), action, username, objectMapper.writeValueAsString(caseData)));
    }
}

