package uk.gov.digital.ho.hocs.casework.domain.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CaseOverview {

  @Getter
  private Long id;

  @Getter
  private String caseUuid;

  @Getter
  private String reference;

  @Getter
  private String caseType;

  @Getter
  private String stageUuid;

  @Getter
  private String stageType;

  @Getter
  private String teamUuid;

  @Getter
  private String teamName;

  @Getter
  private String allocatedUserUuid;

  @Getter
  private String allocatedUserEmail;

  @Getter
  private String ownerUuid;

  @Getter
  private String ownerEmail;

  @Getter
  private String ownerTeamName;

  @Getter
  private LocalDate created;

  @Getter
  private LocalDate received;

  @Getter
  private LocalDate deadline;

  @Getter
  private long daysUntilDeadline;
}
