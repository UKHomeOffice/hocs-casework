package uk.gov.digital.ho.hocs.casework.client.documentclient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.time.LocalDateTime;
import java.util.*;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentClientTest {


    @Mock
    private RestHelper restHelper;

    private DocumentClient documentClient;
    private final String caseType = "MIN";
    private final UUID caseUUID = randomUUID();
    private final UUID uploaderUUID = UUID.randomUUID();
    private final UUID documentUUID = UUID.randomUUID();
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

    private String documentService = "http://document-service";
    ;

    @Before
    public void setUp() {
        documentClient = new DocumentClient(restHelper, documentService);
        documentDto = new DocumentDto(documentUUID, caseUUID, docType, docDisplayName, docStatus, docCreated, docUpdated,uploaderUUID, docDeleted, docLabels, true, true);
        s3Document = new S3Document(docDisplayName, docOriginalName, new byte[10], fileType, mimeType);
    }

    @Test
    public void getDocuments() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(new HashSet<>(Arrays.asList(documentDto)), new ArrayList<String>(Arrays.asList("ORIGINAL", "DRAFT")));
        String url = "/document/reference/" + caseUUID.toString();
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(documentsResponse);

        GetDocumentsResponse actualResult = documentClient.getDocuments(caseUUID, null);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getDocumentDtos()).isNotNull();
        assertThat(actualResult.getDocumentDtos().size()).isOne();
        assertThat(actualResult.getDocumentTags()).isNotNull();
        assertThat(actualResult.getDocumentTags().size()).isEqualTo(2);
        assertThat(actualResult.getDocumentTags().get(0)).isEqualTo("ORIGINAL");
        assertThat(actualResult.getDocumentTags().get(1)).isEqualTo("DRAFT");
        checkDocumentDto(actualResult.getDocumentDtos().iterator().next());
        verify(restHelper).get(documentService, url, GetDocumentsResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocuments_shouldPopulateType() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(new HashSet<>(Arrays.asList(documentDto)), new ArrayList<String>(Arrays.asList("ORIGINAL", "DRAFT")));
        String url = "/document/reference/" + caseUUID.toString() + "/?type=" + caseType;
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(documentsResponse);

        GetDocumentsResponse actualResult = documentClient.getDocuments(caseUUID, caseType);

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getDocumentDtos()).isNotNull();
        assertThat(actualResult.getDocumentDtos().size()).isOne();
        assertThat(actualResult.getDocumentTags()).isNotNull();
        assertThat(actualResult.getDocumentTags().size()).isEqualTo(2);
        assertThat(actualResult.getDocumentTags().get(0)).isEqualTo("ORIGINAL");
        assertThat(actualResult.getDocumentTags().get(1)).isEqualTo("DRAFT");
        verify(restHelper).get(documentService, url, GetDocumentsResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocument() {
        String url = "/document/" + caseUUID.toString();
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(documentDto);

        DocumentDto actualResult = documentClient.getDocument(caseUUID);

        checkDocumentDto(actualResult);
        verify(restHelper).get(documentService, url, DocumentDto.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void deleteDocument() {
        String url = "/document/" + caseUUID.toString();

        documentClient.deleteDocument(caseUUID);

        verify(restHelper).delete(documentService, url);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocumentFile() {
        String url = "/document/" + caseUUID.toString() + "/file";
        when(restHelper.getFile(anyString(), anyString())).thenReturn(s3Document);

        S3Document actualResult = documentClient.getDocumentFile(caseUUID);

        checkS3Document(actualResult);
        verify(restHelper).getFile(documentService, url);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocumentPdf() {
        String url = "/document/" + caseUUID.toString() + "/pdf";
        when(restHelper.getFile(anyString(), anyString())).thenReturn(s3Document);

        S3Document actualResult = documentClient.getDocumentPdf(caseUUID);

        checkS3Document(actualResult);
        verify(restHelper).getFile(documentService, url);
        verifyNoMoreInteractions(restHelper);
    }

    private void checkDocumentDto(DocumentDto result){
        assertThat(result).isNotNull();
        assertThat(result.getUuid()).isEqualTo(documentUUID);
        assertThat(result.getExternalReferenceUUID()).isEqualTo(caseUUID);
        assertThat(result.getType()).isEqualTo(docType);
        assertThat(result.getDisplayName()).isEqualTo(docDisplayName);
        assertThat(result.getStatus()).isEqualTo(docStatus);
        assertThat(result.getCreated()).isEqualTo(docCreated);
        assertThat(result.getUpdated()).isEqualTo(docUpdated);
        assertThat(result.getDeleted()).isEqualTo(docDeleted);
    }

    private void checkS3Document(S3Document result){
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo(docDisplayName);
        assertThat(result.getOriginalFilename()).isEqualTo(docOriginalName);
        assertThat(result.getData()).hasSize(10);
        assertThat(result.getFileType()).isEqualTo(fileType);
        assertThat(result.getMimeType()).isEqualTo(mimeType);
    }

    @Test
    public void getDocumentsForAction() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(
                new HashSet<>(Arrays.asList(documentDto)), new ArrayList<>(Arrays.asList("ACTION_DOC")));

        UUID actionDataUuid = UUID.randomUUID();

        String url = String.format("/document/reference/%s/actionDataUuid/%s/type/%s",
                caseUUID, actionDataUuid, "ACTION_DOC");

        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(documentsResponse);

        GetDocumentsResponse actualResult = documentClient.getDocumentsForAction(caseUUID, actionDataUuid, "ACTION_DOC");

        assertThat(actualResult).isNotNull();
        assertThat(actualResult.getDocumentDtos()).isNotNull();
        assertThat(actualResult.getDocumentDtos().size()).isOne();
        assertThat(actualResult.getDocumentTags()).isNotNull();
        assertThat(actualResult.getDocumentTags().size()).isEqualTo(1);
        assertThat(actualResult.getDocumentTags().get(0)).isEqualTo("ACTION_DOC");
        checkDocumentDto(actualResult.getDocumentDtos().iterator().next());
        verify(restHelper).get(documentService, url, GetDocumentsResponse.class);
        verifyNoMoreInteractions(restHelper);
    }
}
