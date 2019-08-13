package uk.gov.digital.ho.hocs.casework.client.documentclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.application.RestHelper;

import java.util.*;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentClientTest {


    @Mock
    private RestHelper restHelper;

    private DocumentClient documentClient;
    private final String caseType = "MIN";
    private final UUID caseUUID = randomUUID();

    private String documentService = "http://document-service";
    ;

    @Before
    public void setUp() {
        documentClient = new DocumentClient(restHelper, documentService);
    }

    @Test
    public void getDocuments() {
        GetDocumentsResponse mockedResult = mock(GetDocumentsResponse.class);
        String url = "/document/reference/" + caseUUID.toString();
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(mockedResult);

        GetDocumentsResponse actualResult = documentClient.getDocuments(caseUUID, null);

        Assert.assertEquals("Should return mocked Object", mockedResult, actualResult);
        verify(restHelper).get(documentService, url, GetDocumentsResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocuments_shouldPopulateType() {
        GetDocumentsResponse mockedResult = mock(GetDocumentsResponse.class);
        String url = "/document/reference/" + caseUUID.toString() + "/?type=" + caseType;
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(mockedResult);

        GetDocumentsResponse actualResult = documentClient.getDocuments(caseUUID, caseType);

        Assert.assertEquals("Should return mocked Object", mockedResult, actualResult);
        verify(restHelper).get(documentService, url, GetDocumentsResponse.class);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocument() {
        DocumentDto mockedResult = mock(DocumentDto.class);
        String url = "/document/" + caseUUID.toString();
        when(restHelper.get(anyString(), anyString(), any(Class.class))).thenReturn(mockedResult);

        DocumentDto actualResult = documentClient.getDocument(caseUUID);

        Assert.assertEquals("Should return mocked Object", mockedResult, actualResult);
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
        S3Document mockedResult = mock(S3Document.class);
        String url = "/document/" + caseUUID.toString() + "/file";
        when(restHelper.getFile(anyString(), anyString())).thenReturn(mockedResult);
        when(mockedResult.getData()).thenReturn(new byte[10]);

        S3Document actualResult = documentClient.getDocumentFile(caseUUID);

        Assert.assertEquals("Should return mocked Object", mockedResult, actualResult);
        verify(restHelper).getFile(documentService, url);
        verifyNoMoreInteractions(restHelper);
    }

    @Test
    public void getDocumentPdf() {
        S3Document mockedResult = mock(S3Document.class);
        String url = "/document/" + caseUUID.toString() + "/pdf";
        when(restHelper.getFile(anyString(), anyString())).thenReturn(mockedResult);
        when(mockedResult.getData()).thenReturn(new byte[10]);

        S3Document actualResult = documentClient.getDocumentPdf(caseUUID);

        Assert.assertEquals("Should return mocked Object", mockedResult, actualResult);
        verify(restHelper).getFile(documentService, url);
        verifyNoMoreInteractions(restHelper);
    }

}