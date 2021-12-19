package uk.gov.digital.ho.hocs.casework.priority;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.Map;

@Service
public class StagePriorityCalculatorImpl implements StagePriorityCalculator {

    private final StagePriorityPolicyProvider stagePriorityPolicyProvider;

    @Autowired
    public StagePriorityCalculatorImpl(StagePriorityPolicyProvider stagePriorityPolicyProvider) {
        this.stagePriorityPolicyProvider = stagePriorityPolicyProvider;
    }

    @Override
    public void updatePriority(Map<String, String> data, String caseType) {

        data.put(StagePriorityPolicy.CASE_TYPE, caseType);
        double priority = 0;
        for (StagePriorityPolicy policy : stagePriorityPolicyProvider.getPolicies(caseType)) {
            priority += policy.apply(data);
        }
        data.put(SYSTEM_PRIORITY_FIELD_NAME, String.valueOf(priority));

    }
}
