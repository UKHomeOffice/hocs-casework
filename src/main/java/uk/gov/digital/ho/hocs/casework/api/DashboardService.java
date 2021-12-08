package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.utils.DashboardSummaryFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.SummaryRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardSummaryFactory.DashboardSummaryHeaders.*;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class DashboardService {

    private final DashboardSummaryFactory dashboardSummaryFactory;
    private final RequestData requestData;
    private final SummaryRepository summaryRepository;
    private final UserPermissionsService userPermissionsService;

    public DashboardService(DashboardSummaryFactory dashboardSummaryFactory,
                            RequestData requestData,
                            SummaryRepository summaryRepository,
                            UserPermissionsService userPermissionsService) {
        this.dashboardSummaryFactory = dashboardSummaryFactory;
        this.requestData = requestData;
        this.summaryRepository = summaryRepository;
        this.userPermissionsService = userPermissionsService;
    }

    Map<UUID, Map<String, Integer>> getDashboard() {
        log.debug("Getting dashboard summary for user");

        List<Summary> casesSummaries = new ArrayList<>();

        Set<UUID> allUserTeams = userPermissionsService.getUserTeams();
        if(!allUserTeams.isEmpty()) {
            casesSummaries.addAll(summaryRepository.findTeamsAndCaseCountByTeamUuid(allUserTeams));
        } else {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return Collections.emptyMap();
        }

        Set<String> caseTypes = userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin();
        if(caseTypes.size() > 0) {
            casesSummaries.addAll(summaryRepository.findTeamsAndCaseCountByCaseTypes(caseTypes));
        }

        // We need the teams returned by the case summary as that's what the user has access to.
        Set<UUID> dashboardTeams = casesSummaries.stream().map(Summary::getTeamUuid).collect(Collectors.toSet());

        List<Summary> unallocatedCasesSummary = summaryRepository.findUnallocatedCasesByTeam(dashboardTeams);
        List<Summary> overdueCasesSummary = summaryRepository.findOverdueCasesByTeam(dashboardTeams);
        List<Summary> usersCasesSummary = summaryRepository.findUserCasesInTeams(dashboardTeams, requestData.userId());
        List<Summary> usersOverdueCasesSummary = summaryRepository.findOverdueUserCasesInTeams(dashboardTeams, requestData.userId());

        var zippedMap = dashboardSummaryFactory.getZippedSummary(casesSummaries,
                Map.of(UNALLOCATED_TEAM_CASES, unallocatedCasesSummary,
                        OVERDUE_TEAM_CASES, overdueCasesSummary,
                        USERS_TEAM_CASES, usersCasesSummary,
                        USERS_OVERDUE_TEAM_CASES, usersOverdueCasesSummary));

        log.info("Returning {} Stages", zippedMap.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));

        return zippedMap;
    }

}
