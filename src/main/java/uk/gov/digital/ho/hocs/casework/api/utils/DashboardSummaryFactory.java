package uk.gov.digital.ho.hocs.casework.api.utils;

import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.domain.model.Summary;

import java.util.*;

@Component
public class DashboardSummaryFactory {

    private DashboardSummaryFactory() { }

    public Map<UUID, Map<String, Integer>> getZippedSummary(List<Summary> stageWithCaseSummaries, Map<DashboardSummaryHeaders, List<Summary>> zippableSummaries) {
        Map<UUID, Map<String, Integer>> dashboardSummary = new HashMap<>(stageWithCaseSummaries.size());

        stageWithCaseSummaries.forEach(casesSummary -> {
            UUID teamUuid = casesSummary.getTeamUuid();

            dashboardSummary.put(teamUuid, new HashMap<>());

            dashboardSummary.get(teamUuid).put(DashboardSummaryHeaders.TEAM_CASES.toString(), casesSummary.getCount());

            zippableSummaries.forEach((K, V) ->
                    dashboardSummary.get(teamUuid).put(K.toString(), getCountFromListByTeamUuid(V, teamUuid)));
        });

        return dashboardSummary;
    }

    private int getCountFromListByTeamUuid(List<Summary> summaries, UUID uuid) {
        Optional<Summary> summary = summaries.stream().filter(summaryValue -> summaryValue.getTeamUuid().equals(uuid)).findFirst();
        if (summary.isPresent()) {
            return summary.get().getCount();
        }
        return 0;
    }

    public enum DashboardSummaryHeaders {
        TEAM_CASES("cases"),
        UNALLOCATED_TEAM_CASES("unallocatedCases"),
        OVERDUE_TEAM_CASES("overdueCases"),
        USERS_TEAM_CASES("usersCases"),
        USERS_OVERDUE_TEAM_CASES("usersOverdueCases");

        private final String value;

        DashboardSummaryHeaders(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
