package uk.gov.digital.ho.hocs.casework.priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

@Service
public class StagePriorityCalculatorImpl implements StagePriorityCalculator {

    private final StagePriorityPolicyProvider stagePriorityPolicyProvider;

    @Autowired
    public StagePriorityCalculatorImpl(StagePriorityPolicyProvider stagePriorityPolicyProvider) {
        this.stagePriorityPolicyProvider = stagePriorityPolicyProvider;
    }

    @Override
    public void updatePriority(StageWithCaseData stageWithCaseData, String caseType) {
        var caseData = stageWithCaseData.getData();

        double priority = 0;
        for (StagePriorityPolicy policy : stagePriorityPolicyProvider.getPolicies(caseType)) {
            priority += policy.apply(stageWithCaseData);
        }

        caseData.put(StagePriorityPolicy.CASE_TYPE, caseType);
        caseData.put(SYSTEM_PRIORITY_FIELD_NAME, String.valueOf(priority));

    }
}
