package uk.gov.digital.ho.hocs.casework.priority.policy;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy.CASE_TYPE;

@RunWith(MockitoJUnitRunner.class)
public class WorkingDaysElapsedPolicyTest {

    private WorkingDaysElapsedPolicy policy;

    private static final String PROPERTY_NAME = "property1";
    private static final String PROPERTY_VALUE = "value1";
    private static final String DATE_FIELD_NAME = "DateFieldName1";
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final int CAP_NUMBER_OF_DAYS = 55;
    private static final double CAP_POINTS_TO_AWARD = 35d;
    private static final double POINTS_TO_AWARD_PER_DAY = 2d;
    private static final String TEST_CASE_TYPE = "CASE_TYPE_1";

    @Mock
    private InfoClient infoClient;

    @Before
    public void before() {
        policy = new WorkingDaysElapsedPolicy(infoClient, PROPERTY_NAME, PROPERTY_VALUE, DATE_FIELD_NAME, DATE_FORMAT, CAP_NUMBER_OF_DAYS,
                CAP_POINTS_TO_AWARD, POINTS_TO_AWARD_PER_DAY);
    }

    @Test
    public void apply_criteriaMatched() {
        LocalDate testDate = LocalDate.now().minusDays(10);
        when(infoClient.getWorkingDaysElapsedForCaseType(TEST_CASE_TYPE, testDate)).thenReturn(10);
        double result = policy.apply(Map.of(CASE_TYPE, TEST_CASE_TYPE, PROPERTY_NAME, PROPERTY_VALUE, DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate)));
        assertThat(result).isEqualTo(20d);

        verify(infoClient).getWorkingDaysElapsedForCaseType(TEST_CASE_TYPE, testDate);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void apply_criteriaMatched_capped() {
        LocalDate testDate = LocalDate.now().minusDays(74);
        when(infoClient.getWorkingDaysElapsedForCaseType(TEST_CASE_TYPE, testDate)).thenReturn(55);
        double result = policy.apply(Map.of(CASE_TYPE, TEST_CASE_TYPE, PROPERTY_NAME, PROPERTY_VALUE, DATE_FIELD_NAME, DateTimeFormatter.ofPattern(DATE_FORMAT).format(testDate)));
        assertThat(result).isEqualTo(35d);

        verify(infoClient).getWorkingDaysElapsedForCaseType(TEST_CASE_TYPE, testDate);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void apply_criteriaNotMatched() {
        double result = policy.apply(Map.of(PROPERTY_NAME, "C"));
        assertThat(result).isEqualTo(0);

        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void apply_propertyMissing() {
        double result = policy.apply(Map.of());
        assertThat(result).isEqualTo(0);

        verifyNoMoreInteractions(infoClient);
    }
}
