package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.Document;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private AuditService auditService;
    @Mock
    private DocumentRepository documentRepository;

    private DocumentService documentService;
    private final String testUser = "Test User";
    private Document document;
    private final UUID uuid = UUID.randomUUID();
    private LocalDateTime now = LocalDateTime.now();

    @Before
    public void setUp() {
        this.documentService = new DocumentService(
                auditService,
                documentRepository
        );
    }

    @Test
    public void shouldCreateDocument() throws EntityCreationException {
        document = new Document(uuid, uuid, "A", "B", now, "pending", Boolean.FALSE);
        documentService.createDocument(document, testUser);

        verify(documentRepository, times(1)).save(isA(Document.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateExceptionOnCreateDocumentWhenDocumentUUIDIsNull() throws EntityCreationException {
        document = new Document(null, null, "A", "B", now, "pending", Boolean.FALSE);
        documentService.createDocument(document, testUser);
    }

    @Test
    public void shouldUpdateDocument() throws EntityCreationException, EntityNotFoundException {
        when(documentRepository.findByDocumentUuid(any())).thenReturn(new Document(uuid, uuid, "A", "B",now, "pending", Boolean.FALSE));

    Document document = documentService.updateDocument(uuid, uuid,"link","link","Fine",testUser);

        verify(documentRepository, times(1)).findByDocumentUuid(uuid);
        verify(documentRepository, times(1)).save(isA(Document.class));

        assertThat(document).isNotNull();
    }
}