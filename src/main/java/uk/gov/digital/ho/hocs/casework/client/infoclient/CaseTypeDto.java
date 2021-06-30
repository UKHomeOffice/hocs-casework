package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseTypeDto {

    //TODO: remove - used in UI
    @JsonProperty("label")
    private String label;

    //TODO: remove - used in UI
    @JsonProperty("value")
    private String value;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("shortCode")
    private String shortCode;

    @JsonProperty("type")
    private String type;
}
