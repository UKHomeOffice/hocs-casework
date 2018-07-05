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
public class DocumentSummary {

    @JsonProperty("documentUUID")
    private UUID documentUUID;

    @JsonProperty("displayName")
    private String documentDisplayName;

    @JsonProperty("type")
    private DocumentType documentType;
}
