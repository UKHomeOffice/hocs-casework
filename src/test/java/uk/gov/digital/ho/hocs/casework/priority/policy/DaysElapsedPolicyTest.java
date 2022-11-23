package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class DaysElapsedPolicyTest {

    private DaysElapsedPolicy policy;

    private CaseData caseData;

    private ActiveStage activeStage;

    private static final String PROPERTY_NAME = "property1";

    private static final String PROPERTY_VALUE = "value1";

    private static final String DATE_FIELD_NAME = "DateFieldName1";

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final int CAP_NUMBER_OF_DAYS = 55;

    private static final double CAP_POINTS_TO_AWARD = 35d;

    private static final double POINTS_TO_AWARD_PER_DAY = 2d;

    @Before
    public void before() {
        var caseUUID = UUID.randomUUID();
        caseData = new CaseData(caseUUID, LocalDateTime.now(), "MIN", "MIN/123456/22", false, new HashMap<>(), null,
            null, null, null, Collections.emptySet(), null, null, LocalDate.now(), false, null, Collections.emptySet(),
            Set.of());

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null, null,
            caseUUID, null, null, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));

        policy = new DaysElapsedPolicy(PROPERTY_NAME, PROPERTY_VALUE, DATE_FIELD_NAME, DATE_FORMAT, CAP_NUMBER_OF_DAYS,
            CAP_POINTS_TO_AWARD, POINTS_TO_AWARD_PER_DAY);
    }

    @Test
    public void apply_criteriaMatched() {
        LocalDate testDate = LocalDate.now().minusDays(10);

        caseData.update(PROPERTY_NAME, PROPERTY_VALUE);
        caseData.update(DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate));

        double result = policy.apply(caseData, activeStage);
        assertThat(result).isEqualTo(20d);
    }

    @Test
    public void apply_criteriaMatched_capped() {
        LocalDate testDate = LocalDate.now().minusDays(55);

        caseData.update(PROPERTY_NAME, PROPERTY_VALUE);
        caseData.update(DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate));

        double result = policy.apply(caseData, activeStage);

        assertThat(result).isEqualTo(35d);
    }

    @Test
    public void apply_criteriaNotMatched() {
        caseData.update(PROPERTY_NAME, "C");

        double result = policy.apply(caseData, activeStage);
        assertThat(result).isZero();
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(caseData, activeStage);
        assertThat(result).isZero();
    }

}
