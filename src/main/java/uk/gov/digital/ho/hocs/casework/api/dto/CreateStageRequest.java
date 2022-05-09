package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@AllArgsConstructor
@Getter
@ToString
public class CreateStageRequest {

    @NotNull
    @JsonProperty("type")
    private String type;

    @JsonProperty("stageUUID")
    private UUID stageUUIDForRecreation;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("allocationType")
    private String allocationType;

    @JsonProperty("transitionNote")
    private UUID transitionNoteUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;
}