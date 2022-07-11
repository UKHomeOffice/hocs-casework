package uk.gov.digital.ho.hocs.casework.priority;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.PriorityPolicies;
import uk.gov.digital.ho.hocs.casework.domain.repository.PriorityPolicyRepository;
import uk.gov.digital.ho.hocs.casework.priority.policy.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityPolicyProviderImplTest {

    private StagePriorityPolicyProviderImpl stagePriorityPolicyProvider;

    @Mock
    PriorityPolicyRepository priorityPolicyRepository;

    @Mock
    WorkingDaysElapsedProvider workingDaysElapsedProvider;

    private static final String CASE_TYPE = "TestCaseType";
    private static final String PROPERTY_NAME_1 = "property1";
    private static final String PROPERTY_VALUE_1 = "propertyValue1";
    private static final String PROPERTY_NAME_2 = "property2";
    private static final String PROPERTY_VALUE_2 = "propertyValue2";
    private static final String PROPERTY_NAME_DATE = "DateReceived";
    private static final String DATE_FIELD_FORMAT = "yyyy-MM-dd";

    @Before
    public void before(){
        stagePriorityPolicyProvider = new StagePriorityPolicyProviderImpl(priorityPolicyRepository, workingDaysElapsedProvider);
    }

    @Test
    public void getPolicies_blank(){
        List<StagePriorityPolicy> results = stagePriorityPolicyProvider.getPolicies(CASE_TYPE);

        assertThat(results).isNotNull();
        assertThat(results.size()).isZero();

        verify(priorityPolicyRepository).getByCaseType(CASE_TYPE);
        verifyNoMoreInteractions(priorityPolicyRepository);
    }


    @Test
    public void getPolicies(){

        PriorityPolicies.PriorityPolicy policy1 = new PriorityPolicies.PriorityPolicy("SimpleStringPropertyPolicy",
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1, "pointsToAward", "2"));
        PriorityPolicies.PriorityPolicy policy2 = new PriorityPolicies.PriorityPolicy("SimpleStringPropertyPolicy",
                Map.of("propertyName", PROPERTY_NAME_2, "propertyValue", PROPERTY_VALUE_2, "pointsToAward", "1"));
        PriorityPolicies.PriorityPolicy policy3 = new PriorityPolicies.PriorityPolicy("JoinedStringPropertyPolicy",
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1,
                        "propertyName2", PROPERTY_NAME_2, "propertyValue2", PROPERTY_VALUE_2, "pointsToAward", "5"));
        PriorityPolicies.PriorityPolicy policy4 = new PriorityPolicies.PriorityPolicy("DaysElapsedPolicy",
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1,
                        "dateFieldName", PROPERTY_NAME_DATE, "dateFormat", DATE_FIELD_FORMAT, "pointsToAwardPerDay", "3",
                        "capNumberOfDays", "40", "capPointsToAward", "25"));
        PriorityPolicies.PriorityPolicy policy5 = new PriorityPolicies.PriorityPolicy("WorkingDaysElapsedPolicy",
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1,
                        "dateFieldName", PROPERTY_NAME_DATE, "dateFormat", DATE_FIELD_FORMAT, "pointsToAwardPerDay", "2",
                        "capNumberOfDays", "7", "capPointsToAward", "12"));

        when(priorityPolicyRepository.getByCaseType(CASE_TYPE))
                .thenReturn(List.of(policy1, policy2, policy3, policy4, policy5));
        List<StagePriorityPolicy> results = stagePriorityPolicyProvider.getPolicies(CASE_TYPE);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(5);
        assertThat(results.get(0)).isInstanceOf(SimpleStringPropertyPolicy.class);
        assertThat(results.get(1)).isInstanceOf(SimpleStringPropertyPolicy.class);
        assertThat(results.get(2)).isInstanceOf(JoinedStringPropertyPolicy.class);
        assertThat(results.get(3)).isInstanceOf(DaysElapsedPolicy.class);
        assertThat(results.get(4)).isInstanceOf(WorkingDaysElapsedPolicy.class);

        SimpleStringPropertyPolicy resultPolicy1 = (SimpleStringPropertyPolicy) results.get(0);
        assertThat(resultPolicy1.getPropertyName()).isEqualTo(PROPERTY_NAME_1);
        assertThat(resultPolicy1.getPropertyValue()).isEqualTo(PROPERTY_VALUE_1);
        assertThat(resultPolicy1.getPointsToAward()).isEqualTo(2d);

        SimpleStringPropertyPolicy resultPolicy2 = (SimpleStringPropertyPolicy) results.get(1);
        assertThat(resultPolicy2.getPropertyName()).isEqualTo(PROPERTY_NAME_2);
        assertThat(resultPolicy2.getPropertyValue()).isEqualTo(PROPERTY_VALUE_2);
        assertThat(resultPolicy2.getPointsToAward()).isEqualTo(1d);

        JoinedStringPropertyPolicy resultPolicy3 = (JoinedStringPropertyPolicy) results.get(2);
        assertThat(resultPolicy3.getFirstPropertyName()).isEqualTo(PROPERTY_NAME_1);
        assertThat(resultPolicy3.getFirstPropertyValue()).isEqualTo(PROPERTY_VALUE_1);
        assertThat(resultPolicy3.getSecondPropertyName()).isEqualTo(PROPERTY_NAME_2);
        assertThat(resultPolicy3.getSecondPropertyValue()).isEqualTo(PROPERTY_VALUE_2);
        assertThat(resultPolicy3.getPointsToAward()).isEqualTo(5d);

        DaysElapsedPolicy resultPolicy4 = (DaysElapsedPolicy) results.get(3);
        assertThat(resultPolicy4.getPropertyName()).isEqualTo(PROPERTY_NAME_1);
        assertThat(resultPolicy4.getPropertyValue()).isEqualTo(PROPERTY_VALUE_1);
        assertThat(resultPolicy4.getDateFieldName()).isEqualTo(PROPERTY_NAME_DATE);
        assertThat(resultPolicy4.getDateFormat()).isEqualTo(DATE_FIELD_FORMAT);
        assertThat(resultPolicy4.getCapNumberOfDays()).isEqualTo(40);
        assertThat(resultPolicy4.getPointsToAwardPerDay()).isEqualTo(3d);
        assertThat(resultPolicy4.getCapPointsToAward()).isEqualTo(25d);

        WorkingDaysElapsedPolicy resultPolicy5 = (WorkingDaysElapsedPolicy) results.get(4);
        assertThat(resultPolicy5.getPropertyName()).isEqualTo(PROPERTY_NAME_1);
        assertThat(resultPolicy5.getPropertyValue()).isEqualTo(PROPERTY_VALUE_1);
        assertThat(resultPolicy5.getDateFieldName()).isEqualTo(PROPERTY_NAME_DATE);
        assertThat(resultPolicy5.getDateFormat()).isEqualTo(DATE_FIELD_FORMAT);
        assertThat(resultPolicy5.getCapNumberOfDays()).isEqualTo(7);
        assertThat(resultPolicy5.getPointsToAwardPerDay()).isEqualTo(2d);
        assertThat(resultPolicy5.getCapPointsToAward()).isEqualTo(12d);

        verify(priorityPolicyRepository).getByCaseType(CASE_TYPE);
        verifyNoMoreInteractions(priorityPolicyRepository);

    }

    @Test(expected = ApplicationExceptions.InvalidPriorityTypeException.class)
    public void getPolicies_throwsWhenInvalidType(){
        PriorityPolicies.PriorityPolicy unsupportedPolicy = new PriorityPolicies.PriorityPolicy("UnsupportedPolicy", Map.of());
        when(priorityPolicyRepository.getByCaseType(CASE_TYPE))
                .thenReturn(List.of(unsupportedPolicy));

        stagePriorityPolicyProvider.getPolicies(CASE_TYPE);

        verify(priorityPolicyRepository).getByCaseType(CASE_TYPE);
        verifyNoMoreInteractions(priorityPolicyRepository);
    }

}
