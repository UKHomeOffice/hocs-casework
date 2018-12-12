package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseDomain;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.queue.CaseConsumer;
import uk.gov.digital.ho.hocs.casework.queue.dto.UpdateCaseDataRequest;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseConsumerTest extends CamelTestSupport {

    private static final String caseQueue = "direct:case-queue";
    private static final String dlq = "mock:case-queue-dlq";
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CaseDataService mockDataService;

    private HocsCaseDomain hocsCaseDomain;


    @Override
    public RouteBuilder createRouteBuilder() {
        HocsCaseContext hocsCaseContext = new HocsCaseContext(mockDataService, null, null);
        hocsCaseDomain = new HocsCaseDomain(hocsCaseContext);

        return new CaseConsumer(
                hocsCaseDomain,
                caseQueue,
                dlq,
                0, 0, 0);
    }

    @Test
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, ApplicationExceptions.EntityCreationException, ApplicationExceptions.EntityNotFoundException {

        UpdateCaseDataRequest request = new UpdateCaseDataRequest(UUID.randomUUID(), new HashMap<>());

        String json = mapper.writeValueAsString(request);
        template.sendBody(caseQueue, json);

        verify(mockDataService, times(1)).updateCaseData(any(), any());
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, ApplicationExceptions.EntityCreationException, ApplicationExceptions.EntityNotFoundException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(caseQueue, json);
        verify(mockDataService, never()).updateCaseData(any(), any());
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException, ApplicationExceptions.EntityCreationException, ApplicationExceptions.EntityNotFoundException {

        UpdateCaseDataRequest request = new UpdateCaseDataRequest(UUID.randomUUID(), new HashMap<>());

        doThrow(ApplicationExceptions.EntityCreationException.class)
                .when(mockDataService).updateCaseData(any(), any());

        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(request);
        template.sendBody(caseQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}
