package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor()
@Getter
public class FieldDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("component")
    private String component;

    @JsonRawValue
    private String[] validation;

    @JsonProperty("props")
    private PropertyDto properties;

    @JsonProperty("summary")
    private boolean summary;

    @JsonProperty("active")
    private boolean active;
}
