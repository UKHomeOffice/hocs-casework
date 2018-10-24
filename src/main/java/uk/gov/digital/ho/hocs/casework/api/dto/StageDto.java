package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class StageDto {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseType")
    private CaseDataType caseType;

    @JsonProperty("data")
    private String data;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("deadline")
    private String deadline;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;


    public static StageDto from(Stage stage) {
        return new StageDto(
                stage.getCaseReference(),
                stage.getCaseType(),
                stage.getData(),
                stage.getUuid(),
                stage.getCreated(),
                stage.getStageType(),
                stage.getDeadline(),
                stage.getCaseUUID(),
                stage.getTeamUUID(),
                stage.getUserUUID());
    }
}