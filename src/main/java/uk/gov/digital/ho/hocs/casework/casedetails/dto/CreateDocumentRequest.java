package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.model.DocumentType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateDocumentRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("type")
    private DocumentType type;

}
