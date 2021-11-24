package uk.gov.digital.ho.hocs.casework.priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.HashMap;
import java.util.Map;

@Service
public class StagePriorityCalculatorImpl implements StagePriorityCalculator {

    private StagePriorityPolicyProvider stagePriorityPolicyProvider;
    private ObjectMapper objectMapper;

    @Autowired
    public StagePriorityCalculatorImpl(StagePriorityPolicyProvider stagePriorityPolicyProvider, ObjectMapper objectMapper) {
        this.stagePriorityPolicyProvider = stagePriorityPolicyProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public void updatePriority(StageWithCaseData stage) {

        String caseType = stage.getCaseDataType();
        Map<String, String> data = new HashMap<>(stage.getDataMap(objectMapper));
        data.put(StagePriorityPolicy.CASE_TYPE, caseType);
        double priority = 0;
        for (StagePriorityPolicy policy : stagePriorityPolicyProvider.getPolicies(caseType)) {
            priority += policy.apply(data);
        }
        stage.update(Map.of(SYSTEM_PRIORITY_FIELD_NAME, String.valueOf(priority)), objectMapper);

    }
}
