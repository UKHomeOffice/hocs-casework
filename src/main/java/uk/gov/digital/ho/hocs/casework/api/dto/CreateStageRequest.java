package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateStageRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

}