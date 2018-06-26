package uk.gov.digital.ho.hocs.casework.caseDetails.model;

import lombok.*;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "case_document")
@Where(clause = "deleted = FALSE")
public class DocumentData implements Serializable {

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
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "s3_orig_link")
    @Getter
    @Setter
    private String s3OrigLink;

    @Column(name = "s3_pdf_link")
    @Getter
    @Setter
    private String s3PdfLink;

    @Column(name = "status")
    @Getter
    @Setter
    private DocumentStatus status = DocumentStatus.PENDING;

    @Column(name = "deleted")
    @Getter
    @Setter
    private Boolean deleted = Boolean.FALSE;

    public void setDeleted() {
        this.deleted = Boolean.TRUE;
    }

    public void setUnDeleted() {
        this.deleted = Boolean.FALSE;
    }

    public DocumentData(UUID caseUUID, UUID documentUUID, String documentDisplayName, DocumentType documentType) {
        this.caseUUID = caseUUID;
        this.documentUUID = documentUUID;
        this.documentDisplayName = documentDisplayName;
        this.documentType = documentType;
    }
}
