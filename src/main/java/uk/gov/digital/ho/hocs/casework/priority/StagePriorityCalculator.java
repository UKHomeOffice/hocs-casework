package uk.gov.digital.ho.hocs.casework.priority;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.priority.policy.StagePriorityPolicy;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class StagePriorityCalculator {

    static String SYSTEM_PRIORITY_FIELD_NAME = "systemCalculatedPriority";

    private StagePriorityPolicyProvider stagePriorityPolicyProvider;
    private ObjectMapper objectMapper;

    @Autowired
    public StagePriorityCalculator(StagePriorityPolicyProvider stagePriorityPolicyProvider, ObjectMapper objectMapper) {
        this.stagePriorityPolicyProvider = stagePriorityPolicyProvider;
        this.objectMapper = objectMapper;
    }

    public void updatePriority(Stage stage) {
        log.info("Updating priority for stage : {}", stage.getCaseUUID());
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
