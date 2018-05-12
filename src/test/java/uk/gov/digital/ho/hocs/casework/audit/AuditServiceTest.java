package uk.gov.digital.ho.hocs.casework.audit;


import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class AuditServiceTest {

    @Mock
    private AuditRepository auditRepository;

    private AuditService auditService;

    @Before
    public void setUp() {
        auditService = new AuditService(auditRepository);
    }

    @Test
    public void createAuditEntry() throws JsonProcessingException {
        String uuid = "1234";
        String action = "TEST";
        String username = "Test User";
        Map<String,String> caseData = new HashMap<>();
        caseData.put("Test","Value");

        this.auditService.createAuditEntry(uuid, action, username, caseData);

        verify(auditRepository).save(any(AuditEntry.class));
    }

}
