package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {

    private final UUID caseUUID = UUID.randomUUID();
    @Mock
    private DocumentDataService documentService;

    private final UUID uuid = UUID.randomUUID();
    private DocumentDataResource documentResource;

    @Before
    public void setUp(){
        documentResource = new DocumentDataResource(documentService);
    }

    @Test
    public void shouldCreateDocument() throws EntityCreationException {
        String documentDisplayName = "A DOC";

        when(documentService.createDocument(uuid, documentDisplayName, DocumentType.ORIGINAL)).thenReturn(new DocumentData(caseUUID, documentDisplayName, DocumentType.ORIGINAL));
        CreateDocumentRequest request = new CreateDocumentRequest(documentDisplayName, DocumentType.ORIGINAL);

        ResponseEntity response = documentResource.createDocument(uuid, request);

        verify(documentService, times(1)).createDocument(uuid, documentDisplayName, DocumentType.ORIGINAL);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateDocumentException() throws EntityCreationException {
        String documentDisplayName = "A DOC";

        doThrow(EntityCreationException.class).when(documentService).createDocument(uuid, documentDisplayName, DocumentType.ORIGINAL);
        CreateDocumentRequest request = new CreateDocumentRequest(documentDisplayName, DocumentType.ORIGINAL);

        ResponseEntity response = documentResource.createDocument(uuid, request);

        verify(documentService, times(1)).createDocument(uuid, documentDisplayName, DocumentType.ORIGINAL);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
