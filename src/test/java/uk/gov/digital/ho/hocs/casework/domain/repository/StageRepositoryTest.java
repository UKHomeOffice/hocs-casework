package uk.gov.digital.ho.hocs.casework.domain.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.BasicStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseLink;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Ignore
@Slf4j
@RunWith(SpringRunner.class)
@DataJpaTest(showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:stage/afterTest.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class StageRepositoryTest {

    private static final long TEN_SECONDS = 10000L;
    private static final UUID TEAM_UUID = UUID.randomUUID();

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private TestEntityManager entityManager;

    @After
    public void teardown() {
        entityManager.clear();
    }

    @Test
    public void findAllActiveByTeamUUID() {
        final int QUERY_RUNS = 10;

        long[] timings = new long[QUERY_RUNS];
        long[] timingsWithValidTeam = new long[QUERY_RUNS];

        // gi
        createLargeDataSet(10000);

        // when
        for (int i = 0; i < QUERY_RUNS; i++) {
            runFindAllActiveByTeamUuidQuery(UUID.randomUUID(), timings, i);
            runFindAllActiveByTeamUuidQuery(TEAM_UUID, timingsWithValidTeam, i);
        }

        // then
        log.info("Execution millis searching random team:{}, ", timings);
        log.info("Execution millis searching known team:{}, ", timingsWithValidTeam);

        for (int i = 0; i < QUERY_RUNS; i++) {
            assertThat("Duration must be less than 10 seconds", timings[i] <= TEN_SECONDS);
            assertThat("Duration must be less than 10 seconds", timingsWithValidTeam[i] <= TEN_SECONDS);
        }
    }

    @Test
    public void findTeamUuidByCaseUuidAndStageUuid() {
        final int QUERY_AMOUNT = 100;
        long[] timings = new long[QUERY_AMOUNT];

        var stages = createCaseWithStages(10000);

        for (int i = 0; i < QUERY_AMOUNT; i++) {
            var stage = stages.get(i);
            var basicStage = runFindTeamUuidByCaseAndStageQuery(stage.getCaseUUID(), stage.getUuid(), timings, i);

            if (stage.getTeamUUID() == null) {
                assertThat("No team uuid should return null", basicStage.getTeamUUID() == null);
            } else {
                assertThat("Uuid should match the projection", stage.getTeamUUID().equals(basicStage.getTeamUUID()));
            }
        }

        for (int i = 0; i < QUERY_AMOUNT; i++) {
            /*
             * This is still not overly representative of current systems,
             * QA with >70k stages, runs these queries at around 1ms.
             * But this should show degradation if the query is changed.
             */
            assertThat("Duration must be less than 2 seconds", timings[i] <= 2000L);
        }
    }

    private void runFindAllActiveByTeamUuidQuery(UUID team, long[] timings, int iteration) {
        log.info("runFindAllActiveByTeamUuidQuery: " +iteration);
        long start = System.currentTimeMillis();
        stageRepository.findAllActiveByTeamUUID(team);
        long finish = System.currentTimeMillis();
        timings[iteration] = finish - start;
    }

    private BasicStage runFindTeamUuidByCaseAndStageQuery(UUID caseUuid, UUID stageUuid, long[] timings, int iteration) {
        long start = System.currentTimeMillis();
        var result = stageRepository.findActiveBasicStageByCaseUuidStageUUID(caseUuid, stageUuid);
        long finish = System.currentTimeMillis();
        timings[iteration] = finish - start;
        return result;
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

    public List<Stage> createCaseWithStages(final int caseAmount) {
        List<Stage> listAddedStages = new ArrayList<>();

        // Add cases
        for (int i = 0; i < caseAmount; i++) {
            CaseData caseData = new CaseData(CaseDataTypeFactory.from("TEST", "a1"), (long) i, LocalDate.of(2000, 12, 31));
            caseData.setCaseDeadline(LocalDate.of(9999, 12, 31));
            entityManager.persist(caseData);

            // add some stage data to parent case
            for (int y = 0; y < 3; y++) {
                String stageType = "stage" + y;
                Stage stage = new Stage(caseData.getUuid(), stageType, y == 2 ? TEAM_UUID : null, null, null);

                entityManager.persist(stage);

                listAddedStages.add(stage);

                log.debug("Added case: {}, stage: {}", caseData.getUuid(), stageType);
            }
        }

        return listAddedStages;
    }
}
