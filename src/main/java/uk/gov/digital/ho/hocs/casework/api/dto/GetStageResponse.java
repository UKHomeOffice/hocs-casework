package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStageResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("deadline")
    private LocalDate deadline;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("teamUUID")
    private UUID teamUUID;

    @JsonProperty("userUUID")
    private UUID userUUID;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseType")
    private String caseDataType;

    @JsonRawValue
    private String data;

    @JsonProperty("active")
    private boolean active;

    public static GetStageResponse from(Stage stage) {

        return new GetStageResponse(
                stage.getUuid(),
                stage.getCreated(),
                stage.getStageType(),
                stage.getDeadline(),
                stage.getCaseUUID(),
                stage.getTeamUUID(),
                stage.getUserUUID(),
                stage.getCaseReference(),
                stage.getCaseDataType(),
                stage.getData(),
                stage.isActive());
    }
}