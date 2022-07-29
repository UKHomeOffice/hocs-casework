package uk.gov.digital.ho.hocs.casework.priority;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator.SYSTEM_PRIORITY_FIELD_NAME;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityCalculatorImplTest {

    private StagePriorityCalculatorImpl stagePriorityCalculator;

    @Mock
    StagePriorityPolicyProvider stagePriorityPolicyProvider;

    StageWithCaseData stage;

    private final String caseType = "Test_Case_Type";

    @Before
    public void before(){
        stagePriorityCalculator = new StagePriorityCalculatorImpl(stagePriorityPolicyProvider);

        stage = new StageWithCaseData();
        stage.setCaseDataType(caseType);

    }

    @Test
    public void updatePriority_noPolicies(){
        stage.putData("PropertyA", "ValueA");

        stagePriorityCalculator.updatePriority(stage, caseType);

        assertTrue(stage.getData().containsKey(SYSTEM_PRIORITY_FIELD_NAME));
        assertEquals("0.0", stage.getData().get(SYSTEM_PRIORITY_FIELD_NAME));
    }

    @Test
    public void updatePriority_withPolicies(){
        stage.putData("PropertyA", "ValueA");

        StagePriorityPolicy policyA = mock(StagePriorityPolicy.class);
        when(policyA.apply(stage)).thenReturn(12d);
        StagePriorityPolicy policyB = mock(StagePriorityPolicy.class);
        when(policyB.apply(stage)).thenReturn(3d);

        when(stagePriorityPolicyProvider.getPolicies(caseType)).thenReturn(List.of(policyA, policyB));

        stagePriorityCalculator.updatePriority(stage, caseType);

        assertTrue(stage.getData().containsKey(SYSTEM_PRIORITY_FIELD_NAME));
        assertEquals("15.0", stage.getData().get(SYSTEM_PRIORITY_FIELD_NAME));

        verify(policyA).apply(stage);
        verify(policyB).apply(stage);

    }
}
