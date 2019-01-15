package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor()
@Getter
public class PropertyDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("label")
    private String label;
}
