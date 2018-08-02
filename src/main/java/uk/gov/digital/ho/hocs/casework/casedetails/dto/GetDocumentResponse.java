package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetDocumentResponse {

    @JsonProperty("type")
    private DocumentType type;

    @JsonProperty("name")
    private String name;

    @JsonProperty("s3_orig_link")
    private String fileLink;

    @JsonProperty("s3_pdf_link")
    private String pdfLink;

    @JsonProperty("status")
    private DocumentStatus status;

    @JsonProperty("document_uuid")
    private UUID uuid;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

    @JsonProperty("deleted")
    private Boolean deleted;

    public static GetDocumentResponse from(DocumentData documentData) {
        return new GetDocumentResponse(
                documentData.getType(),
                documentData.getName(),
                documentData.getFileLink(),
                documentData.getPdfLink(),
                documentData.getStatus(),
                documentData.getUuid(),
                documentData.getTimestamp(),
                documentData.getDeleted()
        );
    }
}
