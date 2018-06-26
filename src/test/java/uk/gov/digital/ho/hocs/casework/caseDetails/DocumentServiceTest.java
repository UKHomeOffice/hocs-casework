package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    public static final String DOCUMENT_DISPLAY_NAME = "A Document";
    public static final String S3_LINK = "S3LINK";
    @Mock
    private AuditService auditService;
    @Mock
    private DocumentRepository documentRepository;

    private DocumentService documentService;
    private final String testUser = "Test User";
    private DocumentData documentData;
    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.documentService = new DocumentService(
                auditService,
                documentRepository
        );
    }

    @Test
    public void shouldADDDocument() throws EntityCreationException {
        documentService.addDocument(uuid, uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL, testUser);

        verify(documentRepository, times(1)).save(isA(DocumentData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateExceptionOnAddDocumentWhenDocumentUUIDIsNull() throws EntityCreationException {
        documentService.addDocument(null, null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL, testUser);
    }

    @Test()
    public void shouldCreateExceptionOnAddDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.addDocument(null, null, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL, testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
//        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }


    @Test
    public void shouldUpdateDocument() throws EntityCreationException, EntityNotFoundException {
        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL));

    DocumentData documentData = documentService.updateDocument(uuid, uuid, S3_LINK, S3_LINK, DocumentStatus.UPLOADED, testUser);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

        assertThat(documentData).isNotNull();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnUpdateDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(null, null, S3_LINK, S3_LINK,DocumentStatus.UPLOADED,testUser);
    }

    @Test()
    public void shouldCreateExceptionOnUpdateDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.updateDocument(null, null, S3_LINK, S3_LINK,DocumentStatus.UPLOADED,testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
//        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnUpdateDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(uuid, uuid, S3_LINK, S3_LINK,DocumentStatus.UPLOADED,testUser);
    }

    @Test
    public void shouldOnDeleteDocumentSetDeleteToTrue() throws EntityCreationException, EntityNotFoundException {
        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL));

        DocumentData documentData = documentService.deleteDocument(uuid, uuid,testUser);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

        assertThat(documentData).isNotNull();
        assertThat(documentData.getDeleted()).isTrue();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnDeleteDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.deleteDocument(null, null,testUser);
    }

    @Test()
    public void shouldCreateExceptionOnDeleteDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.deleteDocument(null, null,testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
//        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnDeleteDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(uuid, uuid,testUser);
    }

    @Test
    public void shouldOnUndeleteDocumentSetDeleteToFalse() throws EntityCreationException, EntityNotFoundException {
        when(documentRepository.findByDocumentUUID(any())).thenReturn(new DocumentData(uuid, uuid, DOCUMENT_DISPLAY_NAME, DocumentType.ORIGINAL));

        DocumentData documentData = documentService.unDeleteDocument(uuid, uuid,testUser);

        verify(documentRepository, times(1)).findByDocumentUUID(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

        assertThat(documentData).isNotNull();
        assertThat(documentData.getDeleted()).isFalse();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldEntityCreateExceptionOnUndeleteDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(null, null,testUser);
    }

    @Test()
    public void shouldCreateExceptionOnUndeleteDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.unDeleteDocument(null, null,testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByDocumentUUID(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
//        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldEntityNotFoundExceptionOnUndeleteDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.unDeleteDocument(uuid, uuid,testUser);
    }
}