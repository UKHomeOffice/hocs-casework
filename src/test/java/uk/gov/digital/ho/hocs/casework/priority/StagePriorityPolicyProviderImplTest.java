package uk.gov.digital.ho.hocs.casework.priority;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PriorityPolicyDto;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.priority.policy.DaysElapsedPolicy;
import uk.gov.digital.ho.hocs.casework.priority.policy.JoinedStringPropertyPolicy;
import uk.gov.digital.ho.hocs.casework.priority.policy.SimpleStringPropertyPolicy;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityPolicyProviderImplTest {

    private StagePriorityPolicyProviderImpl stagePriorityPolicyProvider;

    @Mock
    InfoClient infoClient;

    private static final String CASE_TYPE = "TestCaseType";
    private static final String PROPERTY_NAME_1 = "property1";
    private static final String PROPERTY_VALUE_1 = "propertyValue1";
    private static final String PROPERTY_NAME_2 = "property2";
    private static final String PROPERTY_VALUE_2 = "propertyValue2";
    private static final String PROPERTY_NAME_DATE = "DateReceived";
    private static final String DATE_FIELD_FORMAT = "yyyy-MM-dd";

    @Before
    public void before(){
        stagePriorityPolicyProvider = new StagePriorityPolicyProviderImpl(infoClient);
    }

    @Test
    public void getPolicies_blank(){
        List<StagePriorityPolicy> results = stagePriorityPolicyProvider.getPolicies(CASE_TYPE);

        assertThat(results).isNotNull();
        assertThat(results.size()).isZero();

        verify(infoClient).getPriorityPoliciesForCaseType(CASE_TYPE);
        verifyNoMoreInteractions(infoClient);
    }


    @Test
    public void getPolicies(){

        PriorityPolicyDto policy1 = new PriorityPolicyDto("SimpleStringPropertyPolicy", CASE_TYPE,
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1, "pointsToAward", "2"));
        PriorityPolicyDto policy2 = new PriorityPolicyDto("SimpleStringPropertyPolicy", CASE_TYPE,
                Map.of("propertyName", PROPERTY_NAME_2, "propertyValue", PROPERTY_VALUE_2, "pointsToAward", "1"));
        PriorityPolicyDto policy3 = new PriorityPolicyDto("JoinedStringPropertyPolicy", CASE_TYPE,
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1,
                        "propertyName2", PROPERTY_NAME_2, "propertyValue2", PROPERTY_VALUE_2, "pointsToAward", "5"));
        PriorityPolicyDto policy4 = new PriorityPolicyDto("DaysElapsedPolicy", CASE_TYPE,
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1,
                        "dateFieldName", PROPERTY_NAME_DATE, "dateFormat", DATE_FIELD_FORMAT, "pointsToAwardPerDay", "3",
                        "capNumberOfDays", "40", "capPointsToAward", "25"));


        when(infoClient.getPriorityPoliciesForCaseType(CASE_TYPE)).thenReturn(List.of(policy1, policy2, policy3, policy4));
        List<StagePriorityPolicy> results = stagePriorityPolicyProvider.getPolicies(CASE_TYPE);

        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(4);
        assertThat(results.get(0)).isInstanceOf(SimpleStringPropertyPolicy.class);
        assertThat(results.get(1)).isInstanceOf(SimpleStringPropertyPolicy.class);
        assertThat(results.get(2)).isInstanceOf(JoinedStringPropertyPolicy.class);
        assertThat(results.get(3)).isInstanceOf(DaysElapsedPolicy.class);

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

        verify(infoClient).getPriorityPoliciesForCaseType(CASE_TYPE);
        verifyNoMoreInteractions(infoClient);

    }

    @Test(expected = ApplicationExceptions.InvalidPriorityTypeException.class)
    public void getPolicies_throwsWhenInvalidType(){
        PriorityPolicyDto unsupportedPolicy = new PriorityPolicyDto("UnsupportedPolicy", CASE_TYPE,
                Map.of("propertyName", PROPERTY_NAME_1, "propertyValue", PROPERTY_VALUE_1, "pointsToAward", "2"));

        when(infoClient.getPriorityPoliciesForCaseType(CASE_TYPE)).thenReturn(List.of(unsupportedPolicy));
        stagePriorityPolicyProvider.getPolicies(CASE_TYPE);
    }
}
