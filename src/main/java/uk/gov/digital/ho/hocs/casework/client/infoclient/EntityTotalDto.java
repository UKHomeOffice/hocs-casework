package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EntityTotalDto {

    @JsonProperty("checkSuffix")
    private String checkSuffix;

    @JsonProperty("valueSuffix")
    private String valueSuffix;

    @JsonProperty("fields")
    private String fields;
}
