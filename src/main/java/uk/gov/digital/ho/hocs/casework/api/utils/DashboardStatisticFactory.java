package uk.gov.digital.ho.hocs.casework.api.utils;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.domain.model.Statistic;

import java.util.*;

@Component
public class DashboardStatisticFactory {

    private DashboardStatisticFactory() { }

    public Map<UUID, Map<String, Integer>> getZippedStatistics(List<Statistic> stageWithCaseStatistics, Map<DashboardStatisticHeaders, List<Statistic>> zippableStatistics) {
        Map<UUID, Map<String, Integer>> dashboardStatistics = new HashMap<>(stageWithCaseStatistics.size());

        stageWithCaseStatistics.forEach(casesStatistic -> {
            UUID teamUuid = casesStatistic.getTeamUuid();

            dashboardStatistics.put(teamUuid, new HashMap<>());

            dashboardStatistics.get(teamUuid).put(DashboardStatisticHeaders.TEAM_CASES.toString(), casesStatistic.getCount());

            zippableStatistics.forEach((K, V) ->
                    dashboardStatistics.get(teamUuid).put(K.toString(), getCountFromListByTeamUuid(V, teamUuid)));
        });

        return dashboardStatistics;
    }

    private int getCountFromListByTeamUuid(List<Statistic> statistics, UUID uuid) {
        Optional<Statistic> statistic = statistics.stream().filter(statisticVal -> statisticVal.getTeamUuid().equals(uuid)).findFirst();
        if (statistic.isPresent()) {
            return statistic.get().getCount();
        }
        return 0;
    }

    public enum DashboardStatisticHeaders {
        TEAM_CASES("cases"),
        UNALLOCATED_TEAM_CASES("unallocatedCases"),
        OVERDUE_TEAM_CASES("overdueCases"),
        USERS_TEAM_CASES("usersCases"),
        USERS_OVERDUE_TEAM_CASES("usersOverdueCases");

        private final String value;

        DashboardStatisticHeaders(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
