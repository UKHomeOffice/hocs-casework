package uk.gov.digital.ho.hocs.casework.api.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.Summary;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardSummaryFactory.DashboardSummaryHeaders.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles({"test", "local"})
public class DashboardSummaryFactoryTest {

    @Autowired
    private DashboardSummaryFactory dashboardSummaryFactory;

    @Test
    public void emptyCaseSummary_returnsEmptyMap() {
        assertThat(dashboardSummaryFactory.getZippedSummary(Collections.emptyList(), null))
                .isEqualTo(Collections.emptyMap());
    }

    @Test
    public void caseSummary_noZippableSummaries_returnsOnlyCaseCount() {
        Summary summary = new Summary(UUID.randomUUID(), 2);

        var zippedSummary =
                dashboardSummaryFactory.getZippedSummary(List.of(summary), Collections.emptyMap());

        assertThat(zippedSummary).isNotNull();
        assertThat(zippedSummary.size()).isEqualTo(1);
        assertThat(zippedSummary.get(summary.getTeamUuid()).size()).isEqualTo(1);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
    }

    @Test
    public void caseSummary_withOtherSummaries_returnsZipped() {
        Summary summary = new Summary(UUID.randomUUID(), 2);
        Map<DashboardSummaryFactory.DashboardSummaryHeaders, List<Summary>> otherSummaries = Map.of(OVERDUE_TEAM_CASES,
                        List.of(new Summary(summary.getTeamUuid(), 3)));

        var zippedSummary =
                dashboardSummaryFactory.getZippedSummary(List.of(summary), otherSummaries);

        assertThat(zippedSummary).isNotNull();
        assertThat(zippedSummary.size()).isEqualTo(1);
        assertThat(zippedSummary.get(summary.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(OVERDUE_TEAM_CASES.toString())).isEqualTo(3);
    }

    @Test
    public void caseSummary_withOtherSummariesNotPartOfCase_returns0ForOtherSummary() {
        Summary summary = new Summary(UUID.randomUUID(), 2);
        Map<DashboardSummaryFactory.DashboardSummaryHeaders, List<Summary>> otherSummaries = Map.of(UNALLOCATED_TEAM_CASES,
                List.of(new Summary(UUID.randomUUID(), 3)));

        var zippedSummary =
                dashboardSummaryFactory.getZippedSummary(List.of(summary), otherSummaries);

        assertThat(zippedSummary).isNotNull();
        assertThat(zippedSummary.size()).isEqualTo(1);
        assertThat(zippedSummary.get(summary.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(UNALLOCATED_TEAM_CASES.toString())).isEqualTo(0);
    }

    @Test
    public void caseSummary_withOtherSummariesIncludingCase_returnsZipped() {
        Summary summary = new Summary(UUID.randomUUID(), 2);
        Map<DashboardSummaryFactory.DashboardSummaryHeaders, List<Summary>> otherSummaries = Map.of(UNALLOCATED_TEAM_CASES,
                List.of(new Summary(UUID.randomUUID(), 3), new Summary(summary.getTeamUuid(), 4)));

        var zippedSummary =
                dashboardSummaryFactory.getZippedSummary(List.of(summary), otherSummaries);

        assertThat(zippedSummary).isNotNull();
        assertThat(zippedSummary.size()).isEqualTo(1);
        assertThat(zippedSummary.get(summary.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedSummary.get(summary.getTeamUuid()).get(UNALLOCATED_TEAM_CASES.toString())).isEqualTo(4);

    }
}
