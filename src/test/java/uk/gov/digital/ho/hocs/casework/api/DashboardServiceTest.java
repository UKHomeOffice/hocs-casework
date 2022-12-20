package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.DashboardSummaryFactory;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.Summary;
import uk.gov.digital.ho.hocs.casework.domain.repository.SummaryRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardSummaryFactory.DashboardSummaryHeaders.*;

@RunWith(MockitoJUnitRunner.class)
public class DashboardServiceTest {

    private DashboardService dashboardService;

    @Mock
    private DashboardSummaryFactory dashboardSummaryFactory;

    @Mock
    private RequestData requestData;

    @Mock
    private SummaryRepository summaryRepository;

    @Mock
    private UserPermissionsService userPermissionsService;

    private final UUID userTeamUuid = UUID.randomUUID();

    private final UUID userUuid = UUID.randomUUID();

    private final Set<UUID> setUserTeams = Set.of(userTeamUuid);

    private final Set<UUID> collatedUserTeams = Set.of(userTeamUuid);

    @Before
    public void setUp() {
        dashboardService = new DashboardService(dashboardSummaryFactory, requestData, summaryRepository,
            userPermissionsService);
    }

    @Test
    public void getDashboard_returnsListOfResults() {
        List<Summary> teamWithCaseCountCaseTypes = List.of(new Summary(userTeamUuid, 1));
        List<Summary> teamWithCaseCount = List.of(new Summary(userTeamUuid, 1));
        List<Summary> teamWithOverdueCaseCount = List.of(new Summary(userTeamUuid, 1));
        List<Summary> teamWithUnallocatedCaseCount = List.of(new Summary(userTeamUuid, 2));
        List<Summary> teamWithUserCaseCount = List.of(new Summary(userTeamUuid, 3));
        List<Summary> teamWithUserOverdueCaseCount = List.of(new Summary(userTeamUuid, 4));

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(setUserTeams);

        when(requestData.userId()).thenReturn(UUID.randomUUID().toString());
        when(summaryRepository.findTeamsAndCaseCountByTeamUuid(setUserTeams)).thenReturn(teamWithCaseCount);
        when(summaryRepository.findOverdueCasesByTeam(collatedUserTeams)).thenReturn(teamWithOverdueCaseCount);
        when(summaryRepository.findUnallocatedCasesByTeam(collatedUserTeams)).thenReturn(teamWithUnallocatedCaseCount);
        when(requestData.userId()).thenReturn(userUuid.toString());
        when(summaryRepository.findUserCasesInTeams(collatedUserTeams, userUuid.toString())).thenReturn(
            teamWithUserCaseCount);
        when(summaryRepository.findOverdueUserCasesInTeams(collatedUserTeams, userUuid.toString())).thenReturn(
            teamWithUserOverdueCaseCount);

        Map<DashboardSummaryFactory.DashboardSummaryHeaders, List<Summary>> statisticMap = Map.of(
            UNALLOCATED_TEAM_CASES, teamWithUnallocatedCaseCount, OVERDUE_TEAM_CASES, teamWithOverdueCaseCount,
            USERS_TEAM_CASES, teamWithUserCaseCount, USERS_OVERDUE_TEAM_CASES, teamWithUserOverdueCaseCount);

        Map<UUID, Map<String, Integer>> returnedMap = Map.of(teamWithCaseCount.get(0).getTeamUuid(),
            Map.of(TEAM_CASES.toString(),
                teamWithCaseCount.get(0).getCount() + teamWithCaseCountCaseTypes.get(0).getCount(),
                UNALLOCATED_TEAM_CASES.toString(), teamWithUnallocatedCaseCount.get(0).getCount(),
                OVERDUE_TEAM_CASES.toString(), teamWithOverdueCaseCount.get(0).getCount(), USERS_TEAM_CASES.toString(),
                teamWithUserCaseCount.get(0).getCount(), USERS_OVERDUE_TEAM_CASES.toString(),
                teamWithUserOverdueCaseCount.get(0).getCount()));

        when(dashboardSummaryFactory.getZippedSummary(teamWithCaseCount, statisticMap)).thenReturn(returnedMap);

        var result = dashboardService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.get(userTeamUuid).size()).isEqualTo(5);
    }

    @Test
    public void getDashboard_emptyTeam_returnEmptyMap() {
        when(userPermissionsService.getExpandedUserTeams()).thenReturn(Collections.emptySet());

        var result = dashboardService.getDashboard();

        assertThat(result).isNotNull();
        assertThat(result.size()).isEqualTo(0);
    }

}
