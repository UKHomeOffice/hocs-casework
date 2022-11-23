package uk.gov.digital.ho.hocs.casework.priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

@Service
public class StagePriorityCalculatorImpl implements StagePriorityCalculator {
    private final StagePriorityPolicyProvider stagePriorityPolicyProvider;

    @Autowired
    public StagePriorityCalculatorImpl(StagePriorityPolicyProvider stagePriorityPolicyProvider) {
        this.stagePriorityPolicyProvider = stagePriorityPolicyProvider;
    }

    @Override
    public void updatePriority(CaseData caseData, String caseType) {
        var data = caseData.getDataMap();

        var policyProviders = stagePriorityPolicyProvider.getPolicies(caseType);

        for (var stage : caseData.getActiveStages()) {
            double priority = 0;
            for (StagePriorityPolicy policy : policyProviders) {
                priority += policy.apply(caseData, stage);
            }
            data.put(SYSTEM_PRIORITY_FIELD_NAME, String.valueOf(priority));
        }
    }
}
