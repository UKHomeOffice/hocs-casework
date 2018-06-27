package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentStatus;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;


@Getter
@EqualsAndHashCode (of="caseUUID, documentUUID")
public class AddDocumentToCaseRequest {

    private final String caseUUID;
    private final String documentUUID;
    private final String documentDisplayName;
    private final DocumentType documentType;
    private final String s3OrigLink;
    private final String s3PdfLink;
    private final DocumentStatus status;

    @JsonCreator
    public AddDocumentToCaseRequest(@JsonProperty("caseUUID") String caseUUID,
                                    @JsonProperty("documentUUID") String documentUUID,
                                    @JsonProperty("documentType") String documentDisplayName,
                                    @JsonProperty("documentDisplayName") DocumentType documentType,
                                    @JsonProperty("s3OrigLink") String s3OrigLink,
                                    @JsonProperty("s3PdfLink") String s3PdfLink,
                                    @JsonProperty("status") DocumentStatus status) {
        this.caseUUID = caseUUID;
        this.documentUUID = documentUUID;
        this.documentDisplayName = documentDisplayName;
        this.documentType = documentType;
        this.s3OrigLink = s3OrigLink;
        this.s3PdfLink = s3PdfLink;
        this.status = status;
    }


}
