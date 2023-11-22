package uk.gov.digital.ho.hocs.casework.reports.domain.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "report_work_in_progress")
public class WorkInProgressData implements Serializable {

    @Id
    @Column(name = "case_uuid")
    private UUID caseUUID;

    private String caseReference;

    private String compType;

    private LocalDate dateCreated;

    private LocalDate dateReceived;

    private LocalDate caseDeadline;

    @Column(name = "owning_csu")
    private String owningCSU;

    private String directorate;

    private String businessAreaBasedOnDirectorate;

    private String enquiryReason;

    private String primaryCorrespondentName;

    private String caseSummary;

    private String severity;

    @Column(name = "assigned_user_uuid")
    private UUID assignedUserUUID;

    @Column(name = "assigned_team_uuid")
    private UUID assignedTeamUUID;

    @Column(name = "stage_uuid")
    private UUID stageUUID;

    private String stageType;

    private String caseType;

    private String dueWeek;

}
