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
public class Document implements Serializable {

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
    @NonNull
    private String documentDisplayName;

    @Column(name = "document_type")
    @Getter
    @NonNull
    private String documentType;

    @Column(name = "timestamp")
    @Getter
    @NonNull
    private LocalDateTime timestamp;

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
    @NonNull
    private String status;

    @Column(name = "deleted")
    @Getter
    @Setter
    @NonNull
    private Boolean deleted;

    public Document(UUID caseUUID, UUID documentUUID, String documentDisplayName, String documentType, LocalDateTime timestamp, String status, Boolean deleted) {
        this.caseUUID = caseUUID;
        this.documentUUID = documentUUID;
        this.documentDisplayName = documentDisplayName;
        this.documentType = documentType;
        this.timestamp = timestamp;
        this.status = status;
        this.deleted = deleted;
    }
}
