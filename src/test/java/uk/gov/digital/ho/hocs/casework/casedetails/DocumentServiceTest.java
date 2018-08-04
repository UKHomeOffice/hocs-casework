package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.DocumentRepository;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {


    @Mock
    private AuditService auditService;
    @Mock
    private DocumentRepository documentRepository;

    private DocumentDataService documentService;
    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.documentService = new DocumentDataService(
                documentRepository,
                auditService
        );
    }

    @Test
    public void shouldCreateDocument() throws EntityCreationException {
        documentService.createDocument(uuid, "name", DocumentType.ORIGINAL);

        verify(documentRepository, times(1)).save(isA(DocumentData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldThrowExceptionOnCreateDocumentWhenDocumentUUIDIsNull() throws EntityCreationException {
        documentService.createDocument(null, "name", DocumentType.ORIGINAL);
    }

    @Test()
    public void shouldThrowExceptionOnCreateDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {
        try {
            documentService.createDocument(null, "name", DocumentType.ORIGINAL);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByUuid(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }

    @Test
    public void shouldUpdateDocument() throws EntityCreationException, EntityNotFoundException {
        when(documentRepository.findByUuid(any())).thenReturn(new DocumentData(uuid, DocumentType.ORIGINAL, "name"));

        documentService.updateDocument(uuid, uuid, DocumentStatus.FAILED, "", "");

        verify(documentRepository, times(1)).findByUuid(uuid);
        verify(documentRepository, times(1)).save(isA(DocumentData.class));

    }

    @Test(expected = EntityCreationException.class)
    public void shouldThrowExceptionOnUpdateDocumentWhenDocumentUUIDIsNull() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(null, null, DocumentStatus.UPLOADED, "", "");
    }

    @Test()
    public void shouldThrowExceptionOnUpdateDocumentWhenDocumentUUIDIsNull2() throws EntityNotFoundException {

        try {
            documentService.updateDocument(null, null, DocumentStatus.UPLOADED, "", "");
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(documentRepository, times(0)).findByUuid(any());
        verify(documentRepository, times(0)).save(any(DocumentData.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldThrowEntityNotFoundExceptionOnUpdateDocumentWhenNoDocumentFound() throws EntityCreationException, EntityNotFoundException {
        documentService.updateDocument(uuid, uuid, DocumentStatus.UPLOADED, "", "");
    }

}