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
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateDocumentRequest;
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
    private DocumentDataService mockDataService;

    private HocsCaseDomain hocsCaseDomain;


    @Override
    public RouteBuilder createRouteBuilder() {
        HocsCaseContext hocsCaseContext = new HocsCaseContext(null, null, null, mockDataService, null, null, null);
        hocsCaseDomain = new HocsCaseDomain(hocsCaseContext);

        return new CaseConsumer(
                hocsCaseDomain,
                caseQueue,
                dlq,
                0, 0, 0);
    }

    @Test
    public void shouldCallAddDocumentToCaseService() throws JsonProcessingException, EntityCreationException, EntityNotFoundException {

        UpdateDocumentRequest document = new UpdateDocumentRequest(caseUUID,
                docUUID, "PDF Link", "Orig Link", DocumentStatus.UPLOADED);

        String json = mapper.writeValueAsString(document);
        template.sendBody(caseQueue, json);

        verify(mockDataService, times(1)).updateDocument(document.getUuid(), document.getStatus(), document.getFileLink(), document.getPdfLink());
    }

    @Test
    public void shouldNotProcessMessgeWhenMarshellingFails() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {
        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString("{invalid:invalid}");
        template.sendBody(caseQueue, json);
        verify(mockDataService, never()).updateDocument(any(), any(), any(), any());
        getMockEndpoint(dlq).assertIsSatisfied();
    }

    @Test
    public void shouldTransferToDLQOnFailure() throws JsonProcessingException, InterruptedException, EntityCreationException, EntityNotFoundException {

        UpdateDocumentRequest document = new UpdateDocumentRequest(caseUUID,
                docUUID, "PDF Link", "Orig Link", DocumentStatus.UPLOADED);

        doThrow(EntityCreationException.class)
                .when(mockDataService)
                .updateDocument(any(), any(), any(), any());

        getMockEndpoint(dlq).setExpectedCount(1);
        String json = mapper.writeValueAsString(document);
        template.sendBody(caseQueue, json);
        getMockEndpoint(dlq).assertIsSatisfied();
    }

}