package uk.gov.digital.ho.hocs.casework.audit.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class DocumentAuditEntryTest {

    @Test
    public void shouldConstructAllValues() {

        DocumentData documentData = new DocumentData(UUID.randomUUID(), DocumentType.ORIGINAL, "anyName");

        DocumentAuditEntry documentAuditEntry = DocumentAuditEntry.from(documentData);

        assertThat(documentAuditEntry.getDocumentUUID()).isEqualTo(documentData.getUuid());
        assertThat(documentAuditEntry.getCaseUUID()).isEqualTo(documentData.getCaseUUID());
        assertThat(documentAuditEntry.getDocumentDisplayName()).isEqualTo(documentData.getName());
        assertThat(documentAuditEntry.getDocumentType()).isEqualTo(documentData.getType());
        assertThat(documentAuditEntry.getTimestamp()).isEqualTo(documentData.getTimestamp());
        assertThat(documentAuditEntry.getS3OrigLink()).isNull();
        assertThat(documentAuditEntry.getS3PdfLink()).isNull();
        assertThat(documentAuditEntry.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(documentAuditEntry.getDeleted()).isEqualTo(false);

    }

    @Test
    public void shouldConstructAllValuesNull() {

        DocumentAuditEntry documentAuditEntry = DocumentAuditEntry.from(null);

        assertThat(documentAuditEntry).isNull();
    }

}