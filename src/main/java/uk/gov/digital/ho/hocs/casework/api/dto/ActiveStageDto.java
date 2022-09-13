package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class ActiveStageDto {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stage")
    private String stage;

    @JsonProperty("assignedToUserUUID")
    private UUID assignedToUserUUID;

    @JsonProperty("assignedToTeamUUID")
    private UUID assignedToTeamUUID;

    public static ActiveStageDto from(ActiveStage stage) {
        return new ActiveStageDto(stage.getUuid(), stage.getStageType(), stage.getUserUUID(), stage.getTeamUUID());
    }

}
