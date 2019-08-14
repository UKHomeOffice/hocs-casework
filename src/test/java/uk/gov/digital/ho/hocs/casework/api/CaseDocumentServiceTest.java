package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID documentUUID = UUID.randomUUID();
    private final String caseType = "MIN";
    private final String docType = "DRAFT";
    private final String docDisplayName = "document.doc";
    private final String docOriginalName = "documentOriginal.doc";
    private final String docStatus = "UPLOADED";
    private final String fileType = "doc";
    private final String mimeType = "application/octet-stream";
    private final LocalDateTime docCreated = LocalDateTime.now();
    private final LocalDateTime docUpdated = LocalDateTime.now();
    private final Boolean docDeleted = false;
    private DocumentDto documentDto;
    private S3Document s3Document;

    @Mock
    private DocumentClient documentClient;
    private CaseDocumentService caseDocumentService;

    @Before
    public void setUp() {
        caseDocumentService = new CaseDocumentService(documentClient);
        documentDto = new DocumentDto(documentUUID, caseUUID, docType, docDisplayName, docStatus, docCreated, docUpdated, docDeleted);
        s3Document = new S3Document(docDisplayName, docOriginalName, new byte[10], fileType, mimeType);
    }

    @Test
    public void getDocuments() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(new HashSet<>(Arrays.asList(documentDto)));
        when(documentClient.getDocuments(caseUUID, caseType)).thenReturn(documentsResponse);

        GetDocumentsResponse result = caseDocumentService.getDocuments(caseUUID, caseType);

        verify(documentClient).getDocuments(caseUUID, caseType);
        verifyNoMoreInteractions(documentClient);
        assertThat(result).isNotNull();
        assertThat(result.getDocumentDtos()).isNotNull();
        assertThat(result.getDocumentDtos().size()).isOne();
        checkDocumentDto(result.getDocumentDtos().iterator().next());

    }

    @Test
    public void deleteDocument() {
        caseDocumentService.deleteDocument(documentUUID);

        verify(documentClient).deleteDocument(documentUUID);
        verifyNoMoreInteractions(documentClient);
    }

    @Test
    public void getDocument() {
        when(documentClient.getDocument(documentUUID)).thenReturn(documentDto);

        DocumentDto result = caseDocumentService.getDocument(documentUUID);

        verify(documentClient).getDocument(documentUUID);
        verifyNoMoreInteractions(documentClient);
        checkDocumentDto(result);
    }

    @Test
    public void getDocumentFile() {
        when(documentClient.getDocumentFile(documentUUID)).thenReturn(s3Document);

        S3Document result = caseDocumentService.getDocumentFile(documentUUID);

        verify(documentClient).getDocumentFile(documentUUID);
        verifyNoMoreInteractions(documentClient);
        checkS3Document(result);
    }

    @Test
    public void getDocumentPdf() {
        when(documentClient.getDocumentPdf(documentUUID)).thenReturn(s3Document);

        S3Document result = caseDocumentService.getDocumentPdf(documentUUID);

        verify(documentClient).getDocumentPdf(documentUUID);
        verifyNoMoreInteractions(documentClient);

        checkS3Document(result);
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
        assertThat(result.getData().length).isEqualTo(10);
        assertThat(result.getFileType()).isEqualTo(fileType);
        assertThat(result.getMimeType()).isEqualTo(mimeType);
    }

}