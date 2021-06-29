package uk.gov.digital.ho.hocs.casework.api.overview;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverview;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverviewRaw;

@Service
@AllArgsConstructor
public class OverviewHydrationService {

  protected final InfoClient infoClient;

  public Page<CaseOverview> hydrate(Page<CaseOverviewRaw> page) {
    List<CaseOverview> caseOverviews = page.stream().map(this::hydrate).collect(Collectors.toList());
    return new PageImpl<>(caseOverviews, page.getPageable(), page.getTotalElements());
  }

  private CaseOverview hydrate(CaseOverviewRaw coRaw) {
    String teamName = null;
    String ownerTeamName = null;
    String allocatedUserEmail = null;
    String ownerUserEmail = null;
    String caseType = null;
    String stageType = null;

    Map<String, UserDto> userMap = infoClient.getUserMap();
    Map<String, TeamDto> teamMap = infoClient.getTeamMap();
    Map<String, CaseTypeDto> caseTypeMap = infoClient.getCaseTypeMap();
    Map<String, StageTypeDto> stageTypeMap = infoClient.getStageTypeMap();

    if (coRaw.getAllocatedUserUuid() != null) {
      UserDto allocatedUser = userMap.get(coRaw.getAllocatedUserUuid());
      if (allocatedUser != null) {
        allocatedUserEmail = allocatedUser.getUsername();
      }
    }
    if (coRaw.getOwnerUuid() != null) {
      UserDto ownerUser = userMap.get(coRaw.getOwnerUuid());
      if (ownerUser != null) {
        ownerUserEmail = ownerUser.getUsername();
      }
    }
    if (coRaw.getTeamUuid() != null) {
      TeamDto team = teamMap.get(coRaw.getTeamUuid());
      if (team != null) {
        teamName = team.getDisplayName();
      }
    }
    if (coRaw.getOwnerTeamUuid() != null) {
      TeamDto ownerTeam = teamMap.get(coRaw.getOwnerTeamUuid());
      if (ownerTeam != null) {
        ownerTeamName = ownerTeam.getDisplayName();
      }
    }
    if (coRaw.getCaseType() != null) {
      CaseTypeDto caseTypeDto = caseTypeMap.get(coRaw.getCaseType());
      if (caseTypeDto != null) {
        caseType = caseTypeDto.getDisplayName();
      }
    }
    if (coRaw.getStageType() != null) {
      StageTypeDto stageTypeDto = stageTypeMap.get(coRaw.getStageType());
      if (stageTypeDto != null) {
        stageType = stageTypeDto.getDisplayName();
      }
    }

    return new CaseOverview(coRaw.getId(), coRaw.getCaseUuid(), coRaw.getReference(), caseType, coRaw.getStageUuid(), stageType, coRaw.getTeamUuid(),
        teamName, coRaw.getAllocatedUserUuid(), allocatedUserEmail, coRaw.getOwnerUuid(), ownerUserEmail, ownerTeamName, coRaw.getCreated(), coRaw.getReceived(), coRaw.getDeadline(), coRaw.getAge(), coRaw.getDaysUntilDeadline());
  }
}
