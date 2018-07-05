package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.DocumentSummary;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    private static final String DOCUMENT_DISPLAY_NAME = "A Document";
    private static final String S3_LINK = "S3LINK";

    @Mock
    private AuditService auditService;
    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private RequestData requestData;

    private DocumentService documentService;
    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.documentService = new DocumentService(
                auditService,
                documentRepository,
                requestData
        );
    }

    @Test
    public void shouldAddDocument() throws EntityCreationException {
        DocumentSummary documentSummary = new DocumentSummary(uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
        documentService.addDocument(uuid, documentSummary);

        verify(documentRepository, times(1)).save(isA(DocumentData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateExceptionOnAddDocumentWhenDocumentUUIDIsNull() throws EntityCreationException {
        DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
        documentService.addDocument(uuid, documentSummary);
    }

    @Test()
    public void shouldCreateExceptionOnAddDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
            documentService.addDocument(uuid, documentSummary);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }

    @Test
    public void shouldAddDocuments() throws EntityCreationException {
        DocumentSummary documentSummary = new DocumentSummary(uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
        documentService.addDocuments(uuid, Arrays.asList(documentSummary));

        verify(documentRepository, times(1)).saveAll(anyCollection());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateExceptionOnAddDocumentsWhenDocumentUUIDIsNull() throws EntityCreationException {
        DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
        documentService.addDocuments(uuid, Arrays.asList(documentSummary));
    }

    @Test()
    public void shouldCreateExceptionOnAddDocumentsWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
            documentService.addDocuments(uuid, Arrays.asList(documentSummary));
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }


    @Test
    public void shouldUpdateDocument() throws EntityCreationException, EntityNotFoundException {
        DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);

        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, documentSummary));

        documentService.updateDocument(uuid, uuid, S3_LINK, S3_LINK, DocumentStatus.UPLOADED);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnUpdateDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(null, null, S3_LINK, S3_LINK,DocumentStatus.UPLOADED);
    }

    @Test()
    public void shouldCreateExceptionOnUpdateDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.updateDocument(null, null, S3_LINK, S3_LINK,DocumentStatus.UPLOADED);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnUpdateDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(uuid, uuid, S3_LINK, S3_LINK,DocumentStatus.UPLOADED);
    }

    @Test
    public void shouldOnDeleteDocumentSetDeleteToTrue() throws EntityCreationException, EntityNotFoundException {
        DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);
        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, documentSummary));

        documentService.deleteDocument(uuid, uuid);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

        //TODO: Captor stuff
        //assertThat(documentData).isNotNull();
        //assertThat(documentData.getDeleted()).isTrue();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnDeleteDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.deleteDocument(null, null);
    }

    @Test()
    public void shouldCreateExceptionOnDeleteDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.deleteDocument(null, null);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnDeleteDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(uuid, uuid);
    }

    @Test
    public void shouldOnUndeleteDocumentSetDeleteToFalse() throws EntityCreationException, EntityNotFoundException {
        DocumentSummary documentSummary = new DocumentSummary(null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL);

        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, documentSummary));

        documentService.unDeleteDocument(uuid, uuid);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

        //TODO: Captor stuff
        //assertThat(documentData).isNotNull();
        //assertThat(documentData.getDeleted()).isFalse();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnUndeleteDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(null, null);
    }

    @Test()
    public void shouldCreateExceptionOnUndeleteDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.unDeleteDocument(null, null);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
//        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnUndeleteDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(uuid, uuid);
    }
}