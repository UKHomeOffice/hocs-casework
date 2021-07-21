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

    @JsonProperty("deadlineWarning")
    private LocalDate deadlineWarning;

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

    @JsonProperty("transitionNote")
    private UUID transitionNoteUUID;

    @JsonRawValue
    private String data;

    @JsonRawValue
    private String somu;

    @JsonRawValue
    private String correspondents;

    @JsonProperty("caseCreated")
    private LocalDateTime caseCreated;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("assignedTopic")
    private String assignedTopic;

    public static GetStageResponse from(Stage stage) {

        return new GetStageResponse(
                stage.getUuid(),
                stage.getCreated(),
                stage.getStageType(),
                stage.getDeadline(),
                stage.getDeadlineWarning(),
                stage.getCaseUUID(),
                stage.getTeamUUID(),
                stage.getUserUUID(),
                stage.getCaseReference(),
                stage.getCaseDataType(),
                stage.getTransitionNoteUUID(),
                stage.getData(),
                stage.getSomu(),
                stage.getCorrespondents(),
                stage.getCaseCreated(),
                stage.isActive(),
                stage.getAssignedTopic());
    }
}
