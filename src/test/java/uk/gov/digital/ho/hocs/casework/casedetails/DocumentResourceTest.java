package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.UpdateDocumentRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentResourceTest {

    @Mock
    private DocumentDataService documentService;

    private DocumentDataResource documentResource;

    @Before
    public void setUp(){
        documentResource = new DocumentDataResource(documentService);
    }

    @Test
    public void shouldCreateDocumentWithValidParams() throws EntityCreationException {

        UUID uuid = UUID.randomUUID();
        String displayName = "name";
        DocumentType documentType = DocumentType.ORIGINAL;
        DocumentData documentData = new DocumentData(uuid, documentType, displayName);

        when(documentService.createDocument(uuid, displayName, documentType)).thenReturn(documentData);

        CreateDocumentRequest request = new CreateDocumentRequest(displayName, documentType);

        ResponseEntity response = documentResource.createDocument(uuid, request);

        verify(documentService, times(1)).createDocument(uuid, displayName, documentType);

        verifyNoMoreInteractions(documentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateDocumentWithValidParams() throws EntityCreationException {

        UUID uuid = UUID.randomUUID();
        DocumentStatus documentStatus = DocumentStatus.UPLOADED;
        String link = "";

        doNothing().when(documentService).updateDocument(uuid, documentStatus, link, link);

        UpdateDocumentRequest request = new UpdateDocumentRequest(link, link, documentStatus);

        ResponseEntity response = documentResource.updateDocument(uuid, uuid, request);

        verify(documentService, times(1)).updateDocument(uuid, documentStatus, link, link);

        verifyNoMoreInteractions(documentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


}
