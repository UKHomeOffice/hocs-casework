package uk.gov.digital.ho.hocs.casework.api.factory;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.CaseCopyStrategy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {
        CaseCopyFactoryTest.StrategyFirst.class,
        CaseCopyFactoryTest.StrategySecond.class,
        CaseCopyFactoryTest.StrategyThird.class,
        CaseCopyFactory.class})
@RunWith(SpringRunner.class)
public class CaseCopyFactoryTest {

    public static final CaseDataType CASE_DATA_TYPE = new CaseDataType("a", "b", "c", "d");
    public static final CaseData CASE = new CaseData(CASE_DATA_TYPE, 1L, LocalDate.now());

    @Autowired
    CaseCopyFactory factory;

    @Before
    public void setUp() {

    }

    @Test
    public void shouldSelectCorrectStrategy() {

        // when
        Optional<CaseCopyStrategy> strategy = factory.getStrategy("A", "B");

        // then
        assertThat(strategy)
                .isNotNull()
                .isPresent();
        assertThat(strategy.get()).isInstanceOf(StrategyFirst.class);

    }

    @Test
    public void canInvokeAStrategy() {

        // when
        Optional<CaseCopyStrategy> strategy = factory.getStrategy("C", "D");

        // then
        assertThat(strategy)
                .isNotNull()
                .isPresent();
        assertThat(strategy.get()).isInstanceOf(StrategyThird.class);

        strategy.get().copyCase(CASE, CASE);
    }

    @Test
    public void shouldNotSelectAStrategyNoMatch() {

        // when
        Optional<CaseCopyStrategy> strategy = factory.getStrategy("X", "Y");

        // then
        assertThat(strategy)
                .isNotNull()
                .isNotPresent();

    }

    @Test
    public void shouldNotSelectAStrategyPartialMatch() {

        // when
        Optional<CaseCopyStrategy> strategy = factory.getStrategy("A", "Y");

        // then
        assertThat(strategy)
                .isNotNull()
                .isNotPresent();

    }

    // Sample Strategy
    @CaseCopy(fromCaseType = "A", toCaseType = "B")
    static class StrategyFirst implements CaseCopyStrategy {

        @Override
        public void copyCase(CaseData fromCase, CaseData toCase) {

        }
    }

    // Sample Strategy - intentionally duplicate caseTypes
    @CaseCopy(fromCaseType = "A", toCaseType = "B")
    static class StrategySecond implements CaseCopyStrategy {

        @Override
        public void copyCase(CaseData fromCase, CaseData toCase) {

        }
    }

    // Sample Strategy
    @CaseCopy(fromCaseType = "C", toCaseType = "D")
    static class StrategyThird implements CaseCopyStrategy {

        @Override
        public void copyCase(CaseData fromCase, CaseData toCase) {

        }
    }

}