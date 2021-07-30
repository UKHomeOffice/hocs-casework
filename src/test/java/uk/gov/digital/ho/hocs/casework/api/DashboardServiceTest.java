package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.DashboardStatisticFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.Statistic;
import uk.gov.digital.ho.hocs.casework.domain.repository.StatisticRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardStatisticFactory.DashboardStatisticHeaders.*;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceTest {

    private DashboardService dashboardService;

    @Mock
    private DashboardStatisticFactory dashboardStatisticFactory;
    @Mock
    private RequestData requestData;
    @Mock
    private StatisticRepository statisticRepository;
    @Mock
    private UserPermissionsService userPermissionsService;

    private final UUID userTeamUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final Set<UUID> setUserTeams = Set.of(userTeamUuid);
    private final Set<String> caseTypes = Set.of("");
    private final Set<UUID> collatedUserTeams = Set.of(userTeamUuid);

    @Before
    public void setUp() {
        dashboardService = new DashboardService(dashboardStatisticFactory, requestData, statisticRepository, userPermissionsService);
    }

    @Test
    public void getDashboard_returnsListOfResults() {
        List<Statistic> teamWithCaseCount = List.of(new Statistic(userTeamUuid, 1));
        List<Statistic> teamWithOverdueCaseCount = List.of(new Statistic(userTeamUuid, 1));
        List<Statistic> teamWithUnallocatedCaseCount = List.of(new Statistic(userTeamUuid, 2));
        List<Statistic> teamWithUserCaseCount = List.of(new Statistic(userTeamUuid, 3));
        List<Statistic> teamWithUserOverdueCaseCount = List.of(new Statistic(userTeamUuid, 4));

        when(userPermissionsService.getUserTeams()).thenReturn(setUserTeams);
        when(userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(caseTypes);

        when(requestData.userId()).thenReturn(UUID.randomUUID().toString());
        when(statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(setUserTeams, caseTypes)).thenReturn(teamWithCaseCount);
        when(statisticRepository.findOverdueCasesByTeam(collatedUserTeams)).thenReturn(teamWithOverdueCaseCount);
        when(statisticRepository.findUnallocatedCasesByTeam(collatedUserTeams)).thenReturn(teamWithUnallocatedCaseCount);
        when(requestData.userId()).thenReturn(userUuid.toString());
        when(statisticRepository.findUserCasesInTeams(collatedUserTeams, userUuid.toString())).thenReturn(teamWithUserCaseCount);
        when(statisticRepository.findOverdueUserCasesInTeams(collatedUserTeams, userUuid.toString())).thenReturn(teamWithUserOverdueCaseCount);

        Map<DashboardStatisticFactory.DashboardStatisticHeaders, List<Statistic>> statisticMap =
                Map.of(UNALLOCATED_TEAM_CASES, teamWithUnallocatedCaseCount,
                        OVERDUE_TEAM_CASES, teamWithOverdueCaseCount,
                        USERS_TEAM_CASES, teamWithUserCaseCount,
                        USERS_OVERDUE_TEAM_CASES, teamWithUserOverdueCaseCount);

        Map<UUID, Map<String, Integer>> returnedMap = Map.of(teamWithCaseCount.get(0).getTeamUuid(),
                Map.of(TEAM_CASES.toString(), teamWithCaseCount.get(0).getCount(),
                        UNALLOCATED_TEAM_CASES.toString(), teamWithUnallocatedCaseCount.get(0).getCount(),
                        OVERDUE_TEAM_CASES.toString(), teamWithOverdueCaseCount.get(0).getCount(),
                        USERS_TEAM_CASES.toString(), teamWithUserCaseCount.get(0).getCount(),
                        USERS_OVERDUE_TEAM_CASES.toString(), teamWithUserOverdueCaseCount.get(0).getCount()));

        when(dashboardStatisticFactory.getZippedStatistics(teamWithCaseCount, statisticMap))
                .thenReturn(returnedMap);

        var result = dashboardService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.get(userTeamUuid).size()).isEqualTo(5);
    }

    @Test
    public void getDashboard_emptyTeam_returnEmptyMap() {
        when(userPermissionsService.getUserTeams()).thenReturn(Collections.emptySet());

        var result = dashboardService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

}
