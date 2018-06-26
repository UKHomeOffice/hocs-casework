package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentRequest;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.AddDocumentResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

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
    private final String testUser = "Test User";

    @Before
    public void setUp(){
        documentResource = new DocumentResource(documentService);
    }

    @Test
    public void shouldAddDocument() throws EntityCreationException {
        String documentDisplayName = "A DOC";
        when(documentService.addDocument(uuid, uuid,documentDisplayName, DocumentType.ORIGINAL,testUser)).thenReturn(new DocumentData(uuid,uuid, documentDisplayName, DocumentType.ORIGINAL));
        AddDocumentRequest request = new AddDocumentRequest(uuid, documentDisplayName, DocumentType.ORIGINAL);

        ResponseEntity<AddDocumentResponse> response = documentResource.AddDocument(uuid, request, testUser);

        verify(documentService, times(1)).addDocument(uuid, uuid, documentDisplayName, DocumentType.ORIGINAL, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(AddDocumentResponse.class);
    }

    @Test
    public void shouldCreateStageException() throws EntityCreationException {
        String documentDisplayName = "A DOC";

        when(documentService.addDocument(uuid, uuid,documentDisplayName, DocumentType.ORIGINAL,testUser)).thenThrow(EntityCreationException.class);
        AddDocumentRequest request = new AddDocumentRequest(uuid, documentDisplayName, DocumentType.ORIGINAL);

        ResponseEntity<AddDocumentResponse> response = documentResource.AddDocument(uuid, request, testUser);

        verify(documentService, times(1)).addDocument(uuid, uuid, documentDisplayName, DocumentType.ORIGINAL, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
