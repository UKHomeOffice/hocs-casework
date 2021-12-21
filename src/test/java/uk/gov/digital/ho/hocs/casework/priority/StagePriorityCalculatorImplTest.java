package uk.gov.digital.ho.hocs.casework.priority;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator.SYSTEM_PRIORITY_FIELD_NAME;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityCalculatorImplTest {

    private StagePriorityCalculatorImpl stagePriorityCalculator;

    @Mock
    StagePriorityPolicyProvider stagePriorityPolicyProvider;

    @Mock
    StageWithCaseData stage;

    private final String caseType = "Test_Case_Type";

    @Before
    public void before(){
        stagePriorityCalculator = new StagePriorityCalculatorImpl(stagePriorityPolicyProvider);
    }

    @Test
    public void updatePriority_noPolicies(){
        var data = new HashMap<>(Map.of("PropertyA", "ValueA"));
        stagePriorityCalculator.updatePriority(data, caseType);

        assertTrue(data.containsKey(SYSTEM_PRIORITY_FIELD_NAME));
        assertEquals("0.0", data.get(SYSTEM_PRIORITY_FIELD_NAME));
    }

    @Test
    public void updatePriority_withPolicies(){
        Map<String, String> dataMap = new HashMap<>(Map.of("PropertyA", "ValueA", StagePriorityPolicy.CASE_TYPE, caseType));

        StagePriorityPolicy policyA = mock(StagePriorityPolicy.class);
        when(policyA.apply(dataMap)).thenReturn(12d);
        StagePriorityPolicy policyB = mock(StagePriorityPolicy.class);
        when(policyB.apply(dataMap)).thenReturn(3d);

        when(stagePriorityPolicyProvider.getPolicies(caseType)).thenReturn(List.of(policyA, policyB));

        stagePriorityCalculator.updatePriority(dataMap, caseType);

        assertTrue(dataMap.containsKey(SYSTEM_PRIORITY_FIELD_NAME));
        assertEquals("15.0", dataMap.get(SYSTEM_PRIORITY_FIELD_NAME));

        verify(policyA).apply(dataMap);
        verify(policyB).apply(dataMap);

    }

    private void checkNoMoreInteraction(){
        verifyNoMoreInteractions(stagePriorityPolicyProvider, stage);
    }
}
