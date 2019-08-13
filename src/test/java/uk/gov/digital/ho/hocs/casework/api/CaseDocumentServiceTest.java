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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDocumentServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID documentUUID = UUID.randomUUID();
    private final String caseType = "MIN";

    @Mock
    private DocumentClient documentClient;
    private CaseDocumentService caseDocumentService;

    @Before
    public void setUp() {
        caseDocumentService = new CaseDocumentService(documentClient);
    }

    @Test
    public void getDocuments() {
        GetDocumentsResponse documentsResponse = mock(GetDocumentsResponse.class);
        when(documentClient.getDocuments(caseUUID, caseType)).thenReturn(documentsResponse);

        GetDocumentsResponse result = caseDocumentService.getDocuments(caseUUID, caseType);

        verify(documentClient).getDocuments(caseUUID, caseType);
        verifyNoMoreInteractions(documentClient);
        assertThat(result).isNotNull();

    }

    @Test
    public void deleteDocument() {
        caseDocumentService.deleteDocument(documentUUID);

        verify(documentClient).deleteDocument(documentUUID);
        verifyNoMoreInteractions(documentClient);
    }

    @Test
    public void getDocument() {
        DocumentDto documentDto = mock(DocumentDto.class);
        when(documentClient.getDocument(documentUUID)).thenReturn(documentDto);

        DocumentDto result = caseDocumentService.getDocument(documentUUID);

        verify(documentClient).getDocument(documentUUID);
        verifyNoMoreInteractions(documentClient);
        assertThat(result).isNotNull();
    }

    @Test
    public void getDocumentFile() {
        S3Document s3Document = mock(S3Document.class);
        when(documentClient.getDocumentFile(documentUUID)).thenReturn(s3Document);

        S3Document result = caseDocumentService.getDocumentFile(documentUUID);

        verify(documentClient).getDocumentFile(documentUUID);
        verifyNoMoreInteractions(documentClient);

        assertThat(result).isNotNull();
    }

    @Test
    public void getDocumentPdf() {
        S3Document s3Document = mock(S3Document.class);
        when(documentClient.getDocumentPdf(documentUUID)).thenReturn(s3Document);

        S3Document result = caseDocumentService.getDocumentPdf(documentUUID);

        verify(documentClient).getDocumentPdf(documentUUID);
        verifyNoMoreInteractions(documentClient);

        assertThat(result).isNotNull();
    }

}