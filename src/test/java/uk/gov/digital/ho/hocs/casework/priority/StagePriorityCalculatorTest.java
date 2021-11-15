package uk.gov.digital.ho.hocs.casework.priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator.SYSTEM_PRIORITY_FIELD_NAME;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityCalculatorTest {

    private StagePriorityCalculator stagePriorityCalculator;

    @Mock
    StagePriorityPolicyProvider stagePriorityPolicyProvider;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    Stage stage;

    private String caseType = "Test_Case_Type";

    @Before
    public void before(){
        when(stage.getCaseDataType()).thenReturn(caseType);
        stagePriorityCalculator = new StagePriorityCalculator(stagePriorityPolicyProvider, objectMapper);
    }

    @Test
    public void updatePriority_noPolicies(){
        Map<String, String> dataMap = Map.of("PropertyA", "ValueA");
        when(stage.getDataMap(objectMapper)).thenReturn(dataMap);

        stagePriorityCalculator.updatePriority(stage);

        verify(stage).getDataMap(objectMapper);
        verify(stage).getCaseDataType();
        verify(stage).getCaseUUID();
        verify(stagePriorityPolicyProvider).getPolicies(caseType);
        verify(stage).update(Map.of(SYSTEM_PRIORITY_FIELD_NAME, "0.0"), objectMapper);

        checkNoMoreInteraction();
    }

    @Test
    public void updatePriority_withPolicies(){
        Map<String, String> dataMap = Map.of("PropertyA", "ValueA", StagePriorityPolicy.CASE_TYPE, caseType);
        when(stage.getDataMap(objectMapper)).thenReturn(dataMap);

        StagePriorityPolicy policyA = mock(StagePriorityPolicy.class);
        when(policyA.apply(dataMap)).thenReturn(12d);
        StagePriorityPolicy policyB = mock(StagePriorityPolicy.class);
        when(policyB.apply(dataMap)).thenReturn(3d);

        when(stagePriorityPolicyProvider.getPolicies(caseType)).thenReturn(List.of(policyA, policyB));

        stagePriorityCalculator.updatePriority(stage);

        verify(stage).getDataMap(objectMapper);
        verify(stage).getCaseDataType();
        verify(stagePriorityPolicyProvider).getPolicies(caseType);
        verify(stage).update(Map.of(SYSTEM_PRIORITY_FIELD_NAME, "15.0"), objectMapper);
        verify(stage).getCaseUUID();
        verify(policyA).apply(dataMap);
        verify(policyB).apply(dataMap);

        checkNoMoreInteraction();
    }

    private void checkNoMoreInteraction(){
        verifyNoMoreInteractions(stagePriorityPolicyProvider, stage, objectMapper);
    }
}
