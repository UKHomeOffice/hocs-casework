package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseDataTag;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetWorkstackResponse {

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

    @JsonProperty("correspondents")
    private Set<WorkstackCorrespondentDto> correspondents;

    @JsonProperty("caseCreated")
    private LocalDateTime caseCreated;

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("assignedTopic")
    private String assignedTopic;

    @JsonProperty("tag")
    private List<String> tag;

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

    public static GetWorkstackResponse from(ActiveStage stage) {
        var tags = stage.getCaseData().getTag().stream().map(CaseDataTag::getTag).toList();

        var correspondents = stage.getCaseData().getCorrespondents().stream().map(
            correspondent -> WorkstackCorrespondentDto.from(correspondent,
                stage.getCaseData().getPrimaryCorrespondentUUID())).collect(Collectors.toSet());

        String topicText = null;
        if (stage.getCaseData().getPrimaryTopic()!=null) {
            topicText = stage.getCaseData().getPrimaryTopic().getText();
        }

        return new GetWorkstackResponse(stage.getUuid(), stage.getCreated(), stage.getStageType(), stage.getDeadline(),
            stage.getDeadlineWarning(), stage.getCaseUUID(), stage.getTeamUUID(), stage.getUserUUID(),
            stage.getCaseData().getReference(), stage.getCaseData().getType(), stage.getTransitionNoteUUID(),
            stage.getCaseData().getDataMap(), null, correspondents, stage.getCaseData().getCreated(), true, topicText,
            tags, stage.getDueContribution(), stage.getContributions(), stage.getNextCaseType(), null, null, null);
    }

}
