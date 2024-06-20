package uk.gov.digital.ho.hocs.casework.reports.domain.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class WorkInProgressRow implements ReportRow, Serializable {

    @JsonProperty("case_uuid")
    private UUID caseUUID;

    @JsonProperty("case_reference")
    private String caseReference;

    @JsonProperty("comp_type")
    private String compType;

    @JsonProperty("date_created")
    private LocalDate dateCreated;

    @JsonProperty("date_received")
    private LocalDate dateReceived;

    @JsonProperty("case_deadline")
    private LocalDate caseDeadline;

    @JsonProperty("owning_csu")
    private String owningCSU;

    @JsonProperty("directorate")
    private String directorate;

    @JsonProperty("business_area_based_on_directorate")
    private String businessAreaBasedOnDirectorate;

    @JsonProperty("enquiry_reason")
    private String enquiryReason;

    @JsonProperty("primary_correspondent_name")
    private String primaryCorrespondentName;

    @JsonProperty("case_summary")
    private String caseSummary;

    @JsonProperty("severity")
    private String severity;

    @JsonProperty("assigned_user_uuid")
    private UUID assignedUserUUID;

    @JsonProperty("assigned_team_uuid")
    private UUID assignedTeamUUID;

    @JsonProperty("stage_uuid")
    private UUID stageUUID;

    @JsonProperty("stage_type")
    private String stageType;

    @JsonProperty("due_week")
    private String dueWeek;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("stage_name")
    private String stageName;

    @JsonProperty("allocation_status")
    private boolean allocationStatus;

    @JsonProperty("allocated_to")
    private String allocatedTo;

}
