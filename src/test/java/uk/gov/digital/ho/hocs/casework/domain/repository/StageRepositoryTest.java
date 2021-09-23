package uk.gov.digital.ho.hocs.casework.domain.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;

@Slf4j
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class StageRepositoryTest {

    private static final long TEN_SECONDS = 10000L;
    private static final int QRY_RUNS = 10;
    private static final UUID TEAM_UUID = UUID.randomUUID();

    @Autowired
    StageRepository stageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void findAllActiveByTeamUUID() {

        long[] timings = new long[QRY_RUNS];
        long[] timingsWithValidTeam = new long[QRY_RUNS];

        // given
        createLargeDataSet(10000);

        // when
        for (int i = 0; i < QRY_RUNS; i++) {
            runQuery(UUID.randomUUID(), timings, i);
            runQuery(TEAM_UUID, timingsWithValidTeam, i);
        }

        // then
        log.info("Execution millis searching random team:{}, ", timings);
        log.info("Execution millis searching known team:{}, ", timingsWithValidTeam);

        for (int i = 0; i < QRY_RUNS; i++) {
            assertThat("Duration must be less than 10 seconds", timings[i] <= TEN_SECONDS);
            assertThat("Duration must be less than 10 seconds", timingsWithValidTeam[i] <= TEN_SECONDS);
        }
    }

    private void runQuery(UUID team, long[] timings, int iteration) {
        long start = System.currentTimeMillis();
        stageRepository.findAllActiveByTeamUUID(team);
        long finish = System.currentTimeMillis();
        timings[iteration] = finish - start;
    }


    private void createLargeDataSet(int howManyCases) {

        UUID prevCaseUuid = null;

        // Add cases
        for (int i = 0; i < howManyCases; i++) {
            CaseData caseData = new CaseData(CaseDataTypeFactory.from("TEST", "a1"), (long) i, LocalDate.of(2000, 12, 31));
            caseData.setCaseDeadline(LocalDate.of(9999, 12, 31));
            entityManager.persist(caseData);

            if (i % 500 == 0) {
                log.info("Added case:{}", i);

            }

            // add some stage data to parent case
            for (int y = 0; y < 10; y++) {
                String stageType = "stage" + y;
                Stage stage = new Stage(caseData.getUuid(), stageType, y == 5 ? TEAM_UUID : null, null, null);
                entityManager.persist(stage);
                log.debug("Added case: {}, stage: {}", caseData.getUuid(), stageType);
            }

            // add link if needs be
            if (i % 9 == 0) {
                prevCaseUuid = caseData.getUuid();
            }

            if (i % 10 == 0) {
                entityManager.persist(new CaseLink(prevCaseUuid, caseData.getUuid()));
                log.debug("Added link from:{} to:{}", prevCaseUuid, caseData.getUuid());
            }

        }
    }
}