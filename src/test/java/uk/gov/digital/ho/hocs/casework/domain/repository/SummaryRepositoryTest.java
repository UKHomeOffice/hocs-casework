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
@ActiveProfiles({"test", "local"})
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@Sql("classpath:sql/summary/init.sql")
@Sql(
        scripts = "classpath:sql/summary/cleandown.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
)
public class SummaryRepositoryTest {

    @Autowired
    private SummaryRepository summaryRepository;

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereNoTeam() {
        var summary =
                summaryRepository.findTeamsAndCaseCountByTeamUuid(Collections.emptySet());

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(0);
    }

    @Test
    public void findTeamsAndCaseCountByTeamUuidandCaseTypes_whereTeamMatchesCase() {
        var summary =
                summaryRepository.findTeamsAndCaseCountByTeamUuid(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")));

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(1);
        assertThat(summary.get(0).getCount()).isEqualTo(2);
    }

    @Test
    public void findOverdueCasesByTeam_whereNoTeam() {
        var summary =
                summaryRepository.findOverdueCasesByTeam(Collections.emptySet());

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(0);
    }

    @Test
    public void findOverdueCasesByTeam_whereTeamMatchesCase() {
        var summary =
                summaryRepository.findOverdueCasesByTeam(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")));

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(1);
        assertThat(summary.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findUnallocatedCasesByTeam_whereNoTeam() {
        var summary =
                summaryRepository.findUnallocatedCasesByTeam(Collections.emptySet());

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(0);
    }

    @Test
    public void findUnallocatedCasesByTeam_whereTeamMatchesCase() {
        var summary =
                summaryRepository.findUnallocatedCasesByTeam(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")));

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(1);
        assertThat(summary.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findUserCasesInTeams_whereNoTeam() {
        var summary =
                summaryRepository.findUserCasesInTeams(Collections.emptySet(), "30000000-0000-0000-0000-000000000001");

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(0);
    }

    @Test
    public void findUserCasesInTeams_whereTeamMatchesCase() {
        var summary =
                summaryRepository.findUserCasesInTeams(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), "30000000-0000-0000-0000-000000000001");

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(1);
        assertThat(summary.get(0).getCount()).isEqualTo(1);
    }

    @Test
    public void findOverdueUserCasesInTeams_whereNoTeam() {
        var summary =
                summaryRepository.findOverdueUserCasesInTeams(Collections.emptySet(), "30000000-0000-0000-0000-000000000001");

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(0);
    }

    @Test
    public void findOverdueUserCasesInTeams_whereTeamMatchesCase() {
        var summary =
                summaryRepository.findOverdueUserCasesInTeams(Set.of(UUID.fromString("20000000-0000-0000-0000-000000000001")), "30000000-0000-0000-0000-000000000001");

        assertThat(summary).isNotNull();
        assertThat(summary.size()).isEqualTo(1);
        assertThat(summary.get(0).getCount()).isEqualTo(1);
    }
    
}
