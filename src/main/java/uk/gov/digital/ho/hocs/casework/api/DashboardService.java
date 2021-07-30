package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.utils.DashboardStatisticFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.StatisticRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardStatisticFactory.DashboardStatisticHeaders.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class DashboardService {

    private final DashboardStatisticFactory dashboardStatisticFactory;
    private final RequestData requestData;
    private final StatisticRepository statisticRepository;
    private final UserPermissionsService userPermissionsService;

    public DashboardService(DashboardStatisticFactory dashboardStatisticFactory,
                            RequestData requestData,
                            StatisticRepository statisticRepository,
                            UserPermissionsService userPermissionsService) {
        this.dashboardStatisticFactory = dashboardStatisticFactory;
        this.requestData = requestData;
        this.statisticRepository = statisticRepository;
        this.userPermissionsService = userPermissionsService;
    }

    Map<UUID, Map<String, Integer>> getDashboard() {
        log.debug("Getting dashboard statistics for user");
        Set<UUID> allUserteams = userPermissionsService.getUserTeams();
        if (allUserteams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return Collections.emptyMap();
        }

        Set<String> caseTypes = userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin();

        List<Statistic> casesStatistics = statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(allUserteams, caseTypes);

        // We need the teams returned by the case statistics as that's what the user has access to.
        Set<UUID> dashboardTeams = casesStatistics.stream().map(Statistic::getTeamUuid).collect(Collectors.toSet());

        List<Statistic> unallocatedCasesStatistic = statisticRepository.findUnallocatedCasesByTeam(dashboardTeams);
        List<Statistic> overdueCasesStatistic = statisticRepository.findOverdueCasesByTeam(dashboardTeams);
        List<Statistic> usersCasesStatistic = statisticRepository.findUserCasesInTeams(dashboardTeams, requestData.userId());
        List<Statistic> usersOverdueCasesStatistic = statisticRepository.findOverdueUserCasesInTeams(dashboardTeams, requestData.userId());

        var zippedMap = dashboardStatisticFactory.getZippedStatistics(casesStatistics,
                Map.of(UNALLOCATED_TEAM_CASES, unallocatedCasesStatistic,
                        OVERDUE_TEAM_CASES, overdueCasesStatistic,
                        USERS_TEAM_CASES, usersCasesStatistic,
                        USERS_OVERDUE_TEAM_CASES, usersOverdueCasesStatistic));

        log.info("Returning {} Stages", zippedMap.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));

        return zippedMap;
    }

}
