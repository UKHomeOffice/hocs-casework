package uk.gov.digital.ho.hocs.casework.reports.domain.reports;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.reports.dto.ReportRow;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class OpenCasesRow implements ReportRow, Serializable {

    @JsonProperty("case_uuid")
    private UUID caseUUID;

    @JsonProperty("case_reference")
    private String caseReference;

    @JsonProperty("business_area")
    private String businessArea;

    @JsonProperty("age")
    private Integer age;

    @JsonProperty("case_deadline")
    private LocalDate caseDeadline;

    @JsonProperty("stage_uuid")
    private UUID stageUUID;

    @JsonProperty("stage_type")
    private String stageType;

    @JsonProperty("assigned_user_uuid")
    private UUID assignedUserUUID;

    @JsonProperty("assigned_team_uuid")
    private UUID assignedTeamUUID;

    @JsonProperty("user_group")
    private String userGroup;

    @JsonProperty("outside_service_standard")
    private Boolean outsideServiceStandard;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("team_name")
    private String teamName;

    @JsonProperty("stage_name")
    private String stageName;
}
