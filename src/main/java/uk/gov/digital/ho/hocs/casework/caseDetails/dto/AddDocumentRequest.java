package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.DocumentType;

import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AddDocumentRequest {

    @JsonProperty("documentUUID")
    private UUID documentUUID;

    @JsonProperty("documentDisplayName")
    private String documentDisplayName;

    @JsonProperty("documentType")
    private DocumentType documentType;
}
