package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentResourceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID documentUUID = UUID.randomUUID();

    private final UUID uploaderUUID = UUID.randomUUID();

    private final String caseType = "MIN";

    private final String docType = "DRAFT";

    private final String docDisplayName = "document.doc";

    private final String docOriginalName = "documentOriginal.doc";

    private final String docStatus = "UPLOADED";

    private final String fileType = "doc";

    private final String mimeType = "application/octet-stream";

    private final Set<String> docLabels = Set.of("Label1", "Label2");

    private final LocalDateTime docCreated = LocalDateTime.now();

    private final LocalDateTime docUpdated = LocalDateTime.now();

    private final Boolean docDeleted = false;

    private DocumentDto documentDto;

    private S3Document s3Document;

    @Mock
    private CaseDocumentService caseDocumentService;

    private CaseDocumentResource caseDocumentResource;

    @Before
    public void setUp() {
        caseDocumentResource = new CaseDocumentResource(caseDocumentService);
        documentDto = new DocumentDto(documentUUID, caseUUID, docType, docDisplayName, docStatus, docCreated,
            docUpdated, uploaderUUID, docDeleted, docLabels, true, true);
        s3Document = new S3Document(docDisplayName, docOriginalName, new byte[10], fileType, mimeType);
    }

    @Test
    public void getDocumentsForCase() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(new HashSet<>(Arrays.asList(documentDto)),
            new ArrayList<String>(Arrays.asList("ORIGINAL", "DRAFT")));

        when(caseDocumentService.getDocuments(caseUUID, caseType)).thenReturn(documentsResponse);

        ResponseEntity<GetDocumentsResponse> response = caseDocumentResource.getDocumentsForCase(caseUUID, caseType);

        verify(caseDocumentService).getDocuments(caseUUID, caseType);
        verifyNoMoreInteractions(caseDocumentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void deleteDocument() {
        ResponseEntity<String> response = caseDocumentResource.deleteDocument(caseUUID, documentUUID);

        verify(caseDocumentService).deleteDocument(documentUUID);
        verifyNoMoreInteractions(caseDocumentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getDocumentResourceLocation() {
        when(caseDocumentService.getDocument(documentUUID)).thenReturn(documentDto);

        ResponseEntity<DocumentDto> response = caseDocumentResource.getDocumentResourceLocation(caseUUID, documentUUID);

        verify(caseDocumentService).getDocument(documentUUID);
        verifyNoMoreInteractions(caseDocumentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getUuid()).isEqualTo(documentUUID);
        assertThat(response.getBody().getExternalReferenceUUID()).isEqualTo(caseUUID);
        assertThat(response.getBody().getType()).isEqualTo(docType);
        assertThat(response.getBody().getDisplayName()).isEqualTo(docDisplayName);
        assertThat(response.getBody().getStatus()).isEqualTo(docStatus);
        assertThat(response.getBody().getCreated()).isEqualTo(docCreated);
        assertThat(response.getBody().getUpdated()).isEqualTo(docUpdated);
        assertThat(response.getBody().getDeleted()).isEqualTo(docDeleted);
    }

    @Test
    public void getCaseDocumentFile() {
        when(caseDocumentService.getDocumentFile(documentUUID)).thenReturn(s3Document);

        ResponseEntity<ByteArrayResource> response = caseDocumentResource.getCaseDocumentFile(caseUUID, documentUUID);

        verify(caseDocumentService).getDocumentFile(documentUUID);
        verifyNoMoreInteractions(caseDocumentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).size()).isOne();
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)).isEqualTo(
            "attachment;filename=" + s3Document.getOriginalFilename());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE).size()).isOne();
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0)).isEqualTo(s3Document.getMimeType());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_LENGTH).get(0)).isEqualTo(
            String.valueOf(s3Document.getData().length));

    }

    @Test
    public void getCaseDocumentPdf() {
        when(caseDocumentService.getDocumentPdf(documentUUID)).thenReturn(s3Document);

        ResponseEntity<ByteArrayResource> response = caseDocumentResource.getCaseDocumentPdf(caseUUID, documentUUID);

        verify(caseDocumentService).getDocumentPdf(documentUUID);
        verifyNoMoreInteractions(caseDocumentService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).size()).isOne();
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION).get(0)).isEqualTo(
            "attachment;filename=" + s3Document.getOriginalFilename());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE).size()).isOne();
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE).get(0)).isEqualTo(s3Document.getMimeType());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_LENGTH).get(0)).isEqualTo(
            String.valueOf(s3Document.getData().length));
    }

}
