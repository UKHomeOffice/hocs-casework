package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateReferenceRequest;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseDomain;
import uk.gov.digital.ho.hocs.casework.queue.CaseConsumer;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseConsumerTest extends CamelTestSupport {

    private static final String caseQueue = "direct:case-queue";
    private static final String dlq = "mock:case-queue-dlq";
    private ObjectMapper mapper = new ObjectMapper();

    private static final UUID caseUUID = UUID.randomUUID();
    private static final UUID docUUID = UUID.randomUUID();


    @Mock
    private ReferenceDataService mockDataService;

    private HocsCaseDomain hocsCaseDomain;


    @Override
    public RouteBuilder createRouteBuilder() {
        HocsCaseContext hocsCaseContext = new HocsCaseContext(null, null, null, null, null, mockDataService, null);
        hocsCaseDomain = new HocsCaseDomain(hocsCaseContext);

        return new CaseConsumer(
                hocsCaseDomain,
                caseQueue,
                dlq,
                0, 0, 0);
    }

    @Test
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, EntityCreationException, EntityNotFoundException {

        CreateReferenceRequest request = new CreateReferenceRequest(UUID.randomUUID(), "fsdfds", ReferenceType.MEMBER_REFERENCE);

        String json = mapper.writeValueAsString(request);
        template.sendBody(caseQueue, json);

        verify(mockDataService, times(1)).createReference(any(), any(), any());
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(caseQueue, json);
        verify(mockDataService, never()).createReference(any(), any(), any());
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {

        CreateReferenceRequest request = new CreateReferenceRequest(UUID.randomUUID(), "fsdfds", ReferenceType.MEMBER_REFERENCE);

        doThrow(EntityCreationException.class)
                .when(mockDataService).createReference(any(), any(), any());

        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(request);
        template.sendBody(caseQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}
