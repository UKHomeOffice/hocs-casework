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
@Table(name = "report_open_cases")
public class OpenCasesData implements Serializable {

    @Id
    @Column(name = "case_uuid")
    private UUID caseUUID;

    private String caseReference;

    private String businessArea;

    private LocalDate dateCreated;

    private Integer age;

    private LocalDate caseDeadline;

    @Column(name = "stage_uuid")
    private UUID stageUUID;

    private String stageType;

    @Column(name = "assigned_user_uuid")
    private UUID assignedUserUUID;

    @Column(name = "assigned_team_uuid")
    private UUID assignedTeamUUID;

    private String caseType;

    private Boolean outsideServiceStandard;

}
