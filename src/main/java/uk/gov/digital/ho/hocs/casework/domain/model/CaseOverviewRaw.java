package uk.gov.digital.ho.hocs.casework.domain.model;

import java.io.Serializable;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "case_overview")
public class CaseOverviewRaw implements Serializable {

  @Id
  @Getter
  private Long id;

  @Getter
  @Column(name = "case_uuid")
  private String caseUuid;

  @Getter
  @Column(name = "case_reference")
  private String reference;

  @Getter
  @Column(name = "case_type")
  private String caseType;

  @Getter
  @Column(name = "stage_uuid")
  private String stageUuid;

  @Getter
  @Column(name = "stage_type")
  private String stageType;

  @Getter
  @Column(name = "team_uuid")
  private String teamUuid;

  @Getter
  @Column(name = "allocated_user_uuid")
  private String allocatedUserUuid;

  @Getter
  @Column(name = "owner_uuid")
  private String ownerUuid;

  @Getter
  @Column(name = "owner_team_uuid")
  private String ownerTeamUuid;

  @Getter
  @Column(name = "created")
  private LocalDate created;

  @Getter
  @Column(name = "date_received")
  private LocalDate received;

  @Getter
  @Column(name = "case_deadline")
  private LocalDate deadline;

  @Getter
  @Column(name = "days_age")
  private long age;

  @Getter
  @Column(name = "days_until_deadline")
  private long daysUntilDeadline;
}
