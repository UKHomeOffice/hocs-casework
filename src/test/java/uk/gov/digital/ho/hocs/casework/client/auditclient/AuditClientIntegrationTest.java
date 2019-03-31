package uk.gov.digital.ho.hocs.casework.client.auditclient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuditClientIntegrationTest extends CamelTestSupport {

    @Mock
    RequestData requestData;

    @Mock
    RestHelper restHelper;

    private final String toEndpoint = "mock:audit-queue";

    ObjectMapper mapper = new ObjectMapper();

    private AuditClient auditClient;
    private static final long caseID = 12345L;
    private final CaseDataType caseType = new CaseDataType("MIN", "a1");
    private LocalDate caseReceived = LocalDate.now();
    private UUID stageUUID = UUID.randomUUID();

    @Before
    public void setup() {
        when(requestData.correlationId()).thenReturn(UUID.randomUUID().toString());
        when(requestData.userId()).thenReturn("some user");
        auditClient = new AuditClient(template, toEndpoint,"hocs-casework","namespace", mapper, requestData, restHelper, "http://audit-service");
    }

    @Test
    public void shouldPutMessageOnAuditQueue() throws InterruptedException, JsonProcessingException {
        CaseData caseData = new CaseData(caseType, caseID, new HashMap<>(), mapper, caseReceived);
        MockEndpoint mockEndpoint = getMockEndpoint(toEndpoint);
        auditClient.updateCaseAudit(caseData);
        mockEndpoint.assertIsSatisfied();
        mockEndpoint.expectedBodyReceived().body().convertToString().contains(String.format("\"reference\":\"%s\"", caseData.getReference()));
    }

}