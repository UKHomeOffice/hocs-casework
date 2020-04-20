package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class UpdateTeamByStageAndTextsRequest {

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("teamUUIDKey")
    private String teamUUIDKey;

    @JsonProperty("teamNameKey")
    private String teamNameKey;

    @JsonProperty("texts")
    private String[] texts;
}
