package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SimpleStageDto {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    public static SimpleStageDto from(ActiveStage activeStage) {

        return new SimpleStageDto(activeStage.getUuid(), activeStage.getStageType(), activeStage.getTeamUUID(),
            activeStage.getUserUUID());
    }

}