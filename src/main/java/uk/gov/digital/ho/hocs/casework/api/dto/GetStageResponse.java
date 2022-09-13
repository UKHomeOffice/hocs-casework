package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Map;
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

    @JsonProperty("data")
    private Map<String, String> data;

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

    @JsonProperty("tag")
    private ArrayList<String> tag;

    @JsonProperty("dueContribution")
    private String dueContribution;

    @JsonProperty("contributions")
    private String contributions;

    @JsonProperty
    private String nextCaseType;

    @JsonProperty
    private String nextCaseReference;

    @JsonProperty
    private String nextCaseUUID;

    @JsonProperty
    private String nextCaseStageUUID;

    public static GetStageResponse from(StageWithCaseData stage) {

        return new GetStageResponse(stage.getUuid(), stage.getCreated(), stage.getStageType(), stage.getDeadline(),
            stage.getDeadlineWarning(), stage.getCaseUUID(), stage.getTeamUUID(), stage.getUserUUID(),
            stage.getCaseReference(), stage.getCaseDataType(), stage.getTransitionNoteUUID(), stage.getData(),
            stage.getSomu(), stage.getCorrespondents(), stage.getCaseCreated(), stage.isActive(),
            stage.getAssignedTopic(), stage.getTag(), stage.getDueContribution(), stage.getContributions(),
            stage.getNextCaseType(), stage.getNextCaseReference(), stage.getNextCaseUUID(),
            stage.getNextCaseStageUUID());
    }

}
