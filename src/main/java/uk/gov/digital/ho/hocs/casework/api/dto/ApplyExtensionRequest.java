package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApplyExtensionRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("caseNote")
    private String caseNote;

}
