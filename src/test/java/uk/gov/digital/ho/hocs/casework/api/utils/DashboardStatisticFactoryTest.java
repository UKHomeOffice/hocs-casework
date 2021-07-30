package uk.gov.digital.ho.hocs.casework.api.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.Statistic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.digital.ho.hocs.casework.api.utils.DashboardStatisticFactory.DashboardStatisticHeaders.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
public class DashboardStatisticFactoryTest {

    @Autowired
    private DashboardStatisticFactory dashboardStatisticFactory;

    @Test
    public void emptyCaseStatistics_returnsEmptyMap() {
        assertThat(dashboardStatisticFactory.getZippedStatistics(Collections.emptyList(), null))
                .isEqualTo(Collections.emptyMap());
    }

    @Test
    public void caseStatistics_noZippableStatistics_returnsOnlyCaseCount() {
        Statistic statistic = new Statistic(UUID.randomUUID(), 2);

        var zippedStatistic =
                dashboardStatisticFactory.getZippedStatistics(List.of(statistic), Collections.emptyMap());

        assertThat(zippedStatistic).isNotNull();
        assertThat(zippedStatistic.size()).isEqualTo(1);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).size()).isEqualTo(1);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
    }

    @Test
    public void caseStatistics_withOtherStatistics_returnsZipped() {
        Statistic statistic = new Statistic(UUID.randomUUID(), 2);
        Map<DashboardStatisticFactory.DashboardStatisticHeaders, List<Statistic>> otherStatistics = Map.of(OVERDUE_TEAM_CASES,
                        List.of(new Statistic(statistic.getTeamUuid(), 3)));

        var zippedStatistic =
                dashboardStatisticFactory.getZippedStatistics(List.of(statistic), otherStatistics);

        assertThat(zippedStatistic).isNotNull();
        assertThat(zippedStatistic.size()).isEqualTo(1);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(OVERDUE_TEAM_CASES.toString())).isEqualTo(3);
    }

    @Test
    public void caseStatistics_withOtherStatisticsNotPartOfCase_returns0ForOtherStatistic() {
        Statistic statistic = new Statistic(UUID.randomUUID(), 2);
        Map<DashboardStatisticFactory.DashboardStatisticHeaders, List<Statistic>> otherStatistics = Map.of(UNALLOCATED_TEAM_CASES,
                List.of(new Statistic(UUID.randomUUID(), 3)));

        var zippedStatistic =
                dashboardStatisticFactory.getZippedStatistics(List.of(statistic), otherStatistics);

        assertThat(zippedStatistic).isNotNull();
        assertThat(zippedStatistic.size()).isEqualTo(1);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(UNALLOCATED_TEAM_CASES.toString())).isEqualTo(0);
    }

    @Test
    public void caseStatistics_withOtherStatisticsIncludingCase_returnsZipped() {
        Statistic statistic = new Statistic(UUID.randomUUID(), 2);
        Map<DashboardStatisticFactory.DashboardStatisticHeaders, List<Statistic>> otherStatistics = Map.of(UNALLOCATED_TEAM_CASES,
                List.of(new Statistic(UUID.randomUUID(), 3), new Statistic(statistic.getTeamUuid(), 4)));

        var zippedStatistic =
                dashboardStatisticFactory.getZippedStatistics(List.of(statistic), otherStatistics);

        assertThat(zippedStatistic).isNotNull();
        assertThat(zippedStatistic.size()).isEqualTo(1);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).size()).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(TEAM_CASES.toString())).isEqualTo(2);
        assertThat(zippedStatistic.get(statistic.getTeamUuid()).get(UNALLOCATED_TEAM_CASES.toString())).isEqualTo(4);

    }
}
