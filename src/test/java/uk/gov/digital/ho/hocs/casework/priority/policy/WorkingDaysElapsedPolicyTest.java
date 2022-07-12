package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkingDaysElapsedPolicyTest {

    private WorkingDaysElapsedPolicy policy;
    private StageWithCaseData stage;


    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "value1";
    private static final String DATE_FIELD_NAME = "DateFieldName1";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int CAP_NUMBER_OF_DAYS = 55;
    private static final double CAP_POINTS_TO_AWARD = 35d;
    private static final double POINTS_TO_AWARD_PER_DAY = 2d;
    private static final String TEST_CASE_TYPE = "CASE_TYPE_1";

    @Mock
    private WorkingDaysElapsedProvider workingDaysElapsedProvider;

    @Before
    public void before() {
        stage = new StageWithCaseData();
        stage.setCaseDataType(TEST_CASE_TYPE);

        policy = new WorkingDaysElapsedPolicy(workingDaysElapsedProvider, PROPERTY_NAME, PROPERTY_VALUE, DATE_FIELD_NAME, DATE_FORMAT, CAP_NUMBER_OF_DAYS,
                CAP_POINTS_TO_AWARD, POINTS_TO_AWARD_PER_DAY);
    }

    @Test
    public void apply_criteriaMatched() {
        LocalDate testDate = LocalDate.now().minusDays(10);

        when(workingDaysElapsedProvider.getWorkingDaysSince(TEST_CASE_TYPE, testDate)).thenReturn(10);

        stage.putData(PROPERTY_NAME, PROPERTY_VALUE);
        stage.putData(DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate));

        double result = policy.apply(stage);
        assertThat(result).isEqualTo(20d);

        verify(workingDaysElapsedProvider).getWorkingDaysSince(TEST_CASE_TYPE, testDate);
        verifyNoMoreInteractions(workingDaysElapsedProvider);
    }

    @Test
    public void apply_criteriaMatched_capped() {
        LocalDate testDate = LocalDate.now().minusDays(74);

        when(workingDaysElapsedProvider.getWorkingDaysSince(TEST_CASE_TYPE, testDate)).thenReturn(55);

        stage.putData(PROPERTY_NAME, PROPERTY_VALUE);
        stage.putData(DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate));

        double result = policy.apply(stage);
        assertThat(result).isEqualTo(35d);

        verify(workingDaysElapsedProvider).getWorkingDaysSince(TEST_CASE_TYPE, testDate);
        verifyNoMoreInteractions(workingDaysElapsedProvider);
    }

    @Test
    public void apply_criteriaNotMatched() {
        stage.putData(PROPERTY_NAME, "C");

        double result = policy.apply(stage);
        assertThat(result).isZero();

        verifyNoMoreInteractions(workingDaysElapsedProvider);
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(stage);
        assertThat(result).isZero();

        verifyNoMoreInteractions(workingDaysElapsedProvider);
    }
}
