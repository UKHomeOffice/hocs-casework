package uk.gov.digital.ho.hocs.casework.api.overview;

import static java.time.temporal.ChronoUnit.DAYS;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
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
import uk.gov.digital.ho.hocs.casework.domain.repository.OverviewRepository;

@Service
public class OverviewService {

  protected final OverviewRepository overviewRepository;
  protected final ObjectMapper objectMapper;
  protected final InfoClient infoClient;
  protected final Map<String, UserDto> userMap = new HashMap<>();
  protected final Map<String, TeamDto> teamMap = new HashMap<>();
  protected final Map<String, CaseTypeDto> caseTypeMap = new HashMap<>();
  protected final Map<String, StageTypeDto> stageTypeMap = new HashMap<>();

  @Autowired
  public OverviewService(OverviewRepository overviewRepository, InfoClient infoClient,
      ObjectMapper objectMapper) {
    this.overviewRepository = overviewRepository;
    this.infoClient = infoClient;
    this.objectMapper = objectMapper;

    infoClient.getUsers().stream().forEach(u -> userMap.put(u.getId(), u));
    infoClient.getTeams().stream().forEach(t -> teamMap.put(t.getUuid().toString(), t));
    infoClient.getCaseTypes().stream().forEach(ct -> caseTypeMap.put(ct.getType(), ct));
    infoClient.getStageTypes().stream().forEach(st -> stageTypeMap.put(st.getType(), st));
  }

  public Page<CaseOverview> getOverview(PageRequest pageRequest) {
    Page<CaseOverviewRaw> unhydratedPage = overviewRepository.findByQuery(pageRequest);
    Page<CaseOverview> hydratedPage = hydrate(unhydratedPage);
    return hydratedPage;
  }

  private Page<CaseOverview> hydrate(Page<CaseOverviewRaw> page) {
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
        teamName, coRaw.getAllocatedUserUuid(), allocatedUserEmail, coRaw.getOwnerUuid(), ownerUserEmail, ownerTeamName, coRaw.getCreated(), coRaw.getReceived(), coRaw.getDeadline(), coRaw.getDaysUntilDeadline());
  }
}
