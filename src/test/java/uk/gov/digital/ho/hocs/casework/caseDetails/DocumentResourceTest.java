package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentsRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {

    @Mock
    private DocumentService documentService;

    private DocumentResource documentResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp(){
        documentResource = new DocumentResource(documentService);
    }

    @Test
    public void shouldAddDocument() throws EntityCreationException {
        String documentDisplayName = "A DOC";
        DocumentSummary documentSummary = new DocumentSummary(uuid, documentDisplayName, DocumentType.ORIGINAL);

        doNothing().when(documentService).addDocument(uuid, documentSummary);
        AddDocumentRequest request = new AddDocumentRequest(documentSummary);

        ResponseEntity response = documentResource.addDocument(uuid, request);

        verify(documentService, times(1)).addDocument(uuid, documentSummary);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAddDocumentException() throws EntityCreationException {
        String documentDisplayName = "A DOC";

        DocumentSummary documentSummary = new DocumentSummary(null, documentDisplayName, DocumentType.ORIGINAL);
        doThrow(EntityCreationException.class).when(documentService).addDocument(uuid, documentSummary);
        AddDocumentRequest request = new AddDocumentRequest(documentSummary);

        ResponseEntity response = documentResource.addDocument(uuid, request);

        verify(documentService, times(1)).addDocument(uuid, documentSummary);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldAddDocuments() throws EntityCreationException {
        String documentDisplayName = "A DOC";
        DocumentSummary documentSummary = new DocumentSummary(uuid, documentDisplayName, DocumentType.ORIGINAL);

        List<DocumentSummary> documentSummaries = Arrays.asList(documentSummary);

        doNothing().when(documentService).addDocuments(uuid, documentSummaries);
        AddDocumentsRequest request = new AddDocumentsRequest(documentSummaries);

        ResponseEntity response = documentResource.addDocuments(uuid, request);

        verify(documentService, times(1)).addDocuments(uuid, documentSummaries);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAddDocumentsException() throws EntityCreationException {
        String documentDisplayName = "A DOC";
        DocumentSummary documentSummary = new DocumentSummary(null, documentDisplayName, DocumentType.ORIGINAL);

        List<DocumentSummary> documentSummaries = Arrays.asList(documentSummary);

        doThrow(EntityCreationException.class).when(documentService).addDocuments(uuid, documentSummaries);
        AddDocumentsRequest request = new AddDocumentsRequest(documentSummaries);

        ResponseEntity response = documentResource.addDocuments(uuid, request);

        verify(documentService, times(1)).addDocuments(uuid, documentSummaries);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
