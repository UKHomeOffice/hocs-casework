package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CaseDataType {

    @JsonProperty("displayCode")
    private String displayCode;

    @JsonProperty("shortCode")
    private String shortCode;
}
