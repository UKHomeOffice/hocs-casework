package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.UUID;

@Getter
public class CorrespondentTypeDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("displayName")
    private String displayName;

    @JsonProperty("type")
    private String type;
}
