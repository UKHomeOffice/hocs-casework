package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentClient;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.S3Document;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

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
    private final Set<String> docLabels = new HashSet<>(Set.of("Label1", "Label2"));
    private final LocalDateTime docCreated = LocalDateTime.now();
    private final LocalDateTime docUpdated = LocalDateTime.now();
    private final Boolean docDeleted = false;
    private DocumentDto documentDto;
    private S3Document s3Document;

    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private DocumentClient documentClient;
    @Mock
    private InfoClient infoClient;

    private ObjectMapper objectMapper;

    private CaseDocumentService caseDocumentService;

    @Before
    public void setUp() {
        objectMapper = new ObjectMapper();
        caseDocumentService = new CaseDocumentService(caseDataRepository, documentClient, infoClient, objectMapper);
        documentDto = new DocumentDto(documentUUID, caseUUID, docType, docDisplayName, docStatus, docCreated, docUpdated, docDeleted, docLabels);
        s3Document = new S3Document(docDisplayName, docOriginalName, new byte[10], fileType, mimeType);
    }

    @Test
    public void getDocuments() {
        GetDocumentsResponse documentsResponse = new GetDocumentsResponse(new HashSet<>(Collections.singletonList(documentDto)), new ArrayList<>(Arrays.asList("ORIGINAL", "DRAFT")));
        CaseDataType type = new CaseDataType("CaseType", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        data.put("DraftDocuments", documentUUID.toString());
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, objectMapper, caseReceived);
        when(caseDataRepository.findAnyByUuid(caseUUID)).thenReturn(caseData);
        when(documentClient.getDocuments(caseUUID, caseType)).thenReturn(documentsResponse);
        when(infoClient.getDocumentTags("CaseType")).thenReturn(new ArrayList<>(Arrays.asList("ORIGINAL", "DRAFT")));

        GetDocumentsResponse result = caseDocumentService.getDocuments(caseUUID, caseType);

        verify(documentClient).getDocuments(caseUUID, caseType);
        verify(caseDataRepository).findAnyByUuid(caseUUID);
        verifyNoMoreInteractions(documentClient, caseDataRepository);
        assertThat(result).isNotNull();
        assertThat(result.getDocumentDtos()).isNotNull();
        assertThat(result.getDocumentDtos().size()).isOne();
        assertThat(result.getDocumentDtos().iterator().next().getLabels()).contains("Primary Draft");
        assertThat(result.getDocumentTags()).isNotNull();
        assertThat(result.getDocumentTags().size()).isEqualTo(2);
        assertThat(result.getDocumentTags().get(0)).isEqualTo("ORIGINAL");
        assertThat(result.getDocumentTags().get(1)).isEqualTo("DRAFT");
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

    private void checkDocumentDto(DocumentDto result) {
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

    private void checkS3Document(S3Document result) {
        assertThat(result).isNotNull();
        assertThat(result.getFilename()).isEqualTo(docDisplayName);
        assertThat(result.getOriginalFilename()).isEqualTo(docOriginalName);
        assertThat(result.getData().length).isEqualTo(10);
        assertThat(result.getFileType()).isEqualTo(fileType);
        assertThat(result.getMimeType()).isEqualTo(mimeType);
    }

}