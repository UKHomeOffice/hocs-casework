package uk.gov.digital.ho.hocs.casework.priority;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator.SYSTEM_PRIORITY_FIELD_NAME;

@RunWith(MockitoJUnitRunner.class)
public class StagePriorityCalculatorImplTest {

    private StagePriorityCalculatorImpl stagePriorityCalculator;

    @Mock
    StagePriorityPolicyProvider stagePriorityPolicyProvider;

    UUID caseUUID = UUID.randomUUID();

    CaseData caseData;

    ActiveStage activeStage;

    private final String caseType = "Test_Case_Type";

    @Before
    public void before() {
        stagePriorityCalculator = new StagePriorityCalculatorImpl(stagePriorityPolicyProvider);

        caseData = new CaseData(caseUUID, LocalDateTime.now(), caseType, "Test_Case_Type/123456/22", false,
            new HashMap<>(), null, null, null, null, Collections.emptySet(), null, null, LocalDate.now(), false, null,
            Collections.emptySet(), Set.of());

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null, null,
            caseUUID, null, null, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));

    }

    @Test
    public void updatePriority_noPolicies() {
        caseData.update("PropertyA", "ValueA");

        stagePriorityCalculator.updatePriority(caseData, caseType);

        assertThat(caseData.getDataMap()).containsEntry(SYSTEM_PRIORITY_FIELD_NAME, "0.0");
    }

    @Test
    public void updatePriority_withPolicies() {
        caseData.update("PropertyA", "ValueA");

        StagePriorityPolicy policyA = mock(StagePriorityPolicy.class);
        when(policyA.apply(caseData, activeStage)).thenReturn(12d);
        StagePriorityPolicy policyB = mock(StagePriorityPolicy.class);
        when(policyB.apply(caseData, activeStage)).thenReturn(3d);

        when(stagePriorityPolicyProvider.getPolicies(caseType)).thenReturn(List.of(policyA, policyB));

        stagePriorityCalculator.updatePriority(caseData, caseType);

        assertThat(caseData.getDataMap()).containsEntry(SYSTEM_PRIORITY_FIELD_NAME, "15.0");

        verify(policyA).apply(caseData, activeStage);
        verify(policyB).apply(caseData, activeStage);

    }

}
