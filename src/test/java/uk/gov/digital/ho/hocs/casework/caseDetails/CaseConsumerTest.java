package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentToCaseRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseConsumerTest extends CamelTestSupport {

    private static final String caseQueue = "direct:case-queue";
    private static final String dlq = "mock:case-queue-dlq";
    private ObjectMapper mapper = new ObjectMapper();

    @Mock
    private CaseDataService mockDataService;

    @Override
    public RouteBuilder createRouteBuilder() {
        return new CaseConsumer(
                mockDataService,
                caseQueue,
                dlq,
                0, 0, 0);
    }

    @Test
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, EntityCreationException, EntityNotFoundException {

        AddDocumentToCaseRequest document = new AddDocumentToCaseRequest("UUID",
                "docUUID", "Test Document", DocumentType.DRAFT,
                "a link", "an original link", DocumentStatus.UPLOADED);

        String json = mapper.writeValueAsString(document);
        template.sendBody(caseQueue, json);

        verify(mockDataService, times(1)).addDocumentToCase(document);
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(caseQueue, json);
        verify(mockDataService, never()).addDocumentToCase(any(AddDocumentToCaseRequest.class));
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {

        AddDocumentToCaseRequest document = new AddDocumentToCaseRequest("UUID",
                "docUUID", "Test Document", DocumentType.DRAFT,
                "a link", "an original link", DocumentStatus.UPLOADED);


        doThrow(EntityCreationException.class)
                .when(mockDataService)
                .addDocumentToCase(any(AddDocumentToCaseRequest.class));

        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(document);
        template.sendBody(caseQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}