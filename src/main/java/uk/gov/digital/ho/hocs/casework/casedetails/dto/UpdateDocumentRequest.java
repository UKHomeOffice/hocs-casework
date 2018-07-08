package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateDocumentRequest {

    @JsonProperty("fileLink")
    private String fileLink;

    @JsonProperty("pdfLink")
    private String pdfLink;

    @JsonProperty("status")
    private DocumentStatus status;

}
