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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CaseConsumerTest extends CamelTestSupport {

    private static final String caseQueue = "direct:case-queue";
    private static final String dlq = "direct:case-queue-dlq";
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
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {

        AddDocumentToCaseRequest document = new AddDocumentToCaseRequest("UUID",
                "docUUID", "Test Document", DocumentType.DRAFT,
                "a link", "an original link", DocumentStatus.UPLOADED);

        String json = mapper.writeValueAsString(document);
        template.sendBody(caseQueue, json);
        assertMockEndpointsSatisfied();
        verify(mockDataService, times(1)).addDocumentToCase(document);
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(caseQueue, json);
        assertMockEndpointsSatisfied();
        verify(mockDataService, never()).addDocumentToCase(any(AddDocumentToCaseRequest.class));
    }

}