package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/statistic/init.sql")
@Sql(
        scripts = "classpath:sql/statistic/cleandown.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class StatisticRepositoryTest {

    @Autowired
    private StatisticRepository statisticRepository;

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereNoTeam() {
        var statistic =
                statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(Collections.emptySet(), Set.of(""));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(0);
    }

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereTeamMatchesCase() {
        var statistic =
                statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), Set.of(""));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(3);
    }

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereTeamMatchesCaseAndCaseTypeAdmin() {
        var statistic =
                statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), Set.of("TEST2"));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(2);
        assertThat(statistic.get(0).getCount()).isEqualTo(3);
        assertThat(statistic.get(1).getCount()).isEqualTo(1);
    }

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereTeamNoMatchesCaseAndCaseTypeAdmin() {
        var statistic =
                statisticRepository.findTeamsAndCaseCountByTeamUuidandCaseTypes(Set.of(UUID.fromString("11111111-0000-0000-0000-000000000001")), Set.of("TEST2"));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findOverdueCasesByTeam_whereNoTeam() {
        var statistic =
                statisticRepository.findOverdueCasesByTeam(Collections.emptySet());

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(0);
    }

    @Test
    public void findOverdueCasesByTeam_whereTeamMatchesCase() {
        var statistic =
                statisticRepository.findOverdueCasesByTeam(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findUnallocatedCasesByTeam_whereNoTeam() {
        var statistic =
                statisticRepository.findUnallocatedCasesByTeam(Collections.emptySet());

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(0);
    }

    @Test
    public void findUnallocatedCasesByTeam_whereTeamMatchesCase() {
        var statistic =
                statisticRepository.findUnallocatedCasesByTeam(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")));

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findUserCasesInTeams_whereNoTeam() {
        var statistic =
                statisticRepository.findUserCasesInTeams(Collections.emptySet(), "30000000-0000-0000-0000-000000000001");

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(0);
    }

    @Test
    public void findUserCasesInTeams_whereTeamMatchesCase() {
        var statistic =
                statisticRepository.findUserCasesInTeams(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), "30000000-0000-0000-0000-000000000001");

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(2);
    }

    @Test
    public void findOverdueUserCasesInTeams_whereNoTeam() {
        var statistic =
                statisticRepository.findOverdueUserCasesInTeams(Collections.emptySet(), "30000000-0000-0000-0000-000000000001");

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(0);
    }

    @Test
    public void findOverdueUserCasesInTeams_whereTeamMatchesCase() {
        var statistic =
                statisticRepository.findOverdueUserCasesInTeams(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), "30000000-0000-0000-0000-000000000001");

        assertThat(statistic).isNotNull();
        assertThat(statistic.size()).isEqualTo(1);
        assertThat(statistic.get(0).getCount()).isEqualTo(1);
    }
    
}
