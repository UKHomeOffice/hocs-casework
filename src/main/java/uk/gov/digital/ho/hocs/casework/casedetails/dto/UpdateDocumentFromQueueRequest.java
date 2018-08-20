package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UpdateDocumentFromQueueRequest {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("case")
    private UUID caseUUID;

    @JsonProperty("fileLink")
    private String fileLink;

    @JsonProperty("pdfLink")
    private String pdfLink;

    @JsonProperty("status")
    private DocumentStatus status;
}