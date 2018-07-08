package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_document_data")
@NoArgsConstructor
public class DocumentAuditEntry {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "case_uuid")
    @Getter
    private UUID caseUUID;

    @Column(name = "document_uuid")
    @Getter
    private UUID documentUUID;

    @Column(name = "document_display_name")
    @Getter
    private String documentDisplayName;

    @Column(name = "document_type")
    @Getter
    private DocumentType documentType;

    @Column(name = "timestamp")
    @Getter
    private LocalDateTime timestamp;

    @Column(name = "s3_orig_link")
    @Getter
    private String s3OrigLink;

    @Column(name = "s3_pdf_link")
    @Getter
    private String s3PdfLink;

    @Column(name = "status")
    @Getter
    private DocumentStatus status;

    @Column(name = "deleted")
    @Getter
    private Boolean deleted;

    private DocumentAuditEntry(UUID caseUUID,
                               UUID documentUUID,
                               String documentDisplayName,
                               DocumentType documentType,
                               LocalDateTime timestamp,
                               String s3OrigLink,
                               String s3PdfLink,
                               DocumentStatus status,
                               Boolean deleted) {
        this.caseUUID = caseUUID;
        this.documentUUID = documentUUID;
        this.documentDisplayName = documentDisplayName;
        this.documentType = documentType;
        this.timestamp = timestamp;
        this.s3OrigLink = s3OrigLink;
        this.s3PdfLink = s3PdfLink;
        this.status = status;
        this.deleted = deleted;
    }

    public static DocumentAuditEntry from(DocumentData documentData) {
        return new DocumentAuditEntry(documentData.getCaseUUID(),
                documentData.getDocumentUUID(),
                documentData.getDocumentDisplayName(),
                documentData.getDocumentType(),
                documentData.getTimestamp(),
                documentData.getS3OrigLink(),
                documentData.getS3PdfLink(),
                documentData.getStatus(),
                documentData.getDeleted());
    }
}
