package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class CreateStageRequest {

    @JsonProperty("type")
    private String type;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("allocationType")
    private String allocationType;

    @JsonProperty("transitionNote")
    private UUID transitionNoteUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;
}