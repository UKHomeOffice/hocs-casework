package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.PriorityPolicies;

import java.util.List;

@Service
public class PriorityPolicyRepository extends JsonConfigFileReader {

    private final PriorityPolicies priorityPolicies;

    public PriorityPolicyRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        priorityPolicies = readValueFromFile(new TypeReference<>() {});
    }

    @Override
    String getFileName() {
        return "priority-policies";
    }

    public List<PriorityPolicies.PriorityPolicy> getByCaseType(String caseType) {
        var casePriorities = priorityPolicies.getPoliciesForCaseType(caseType);

        if (casePriorities == null) {
            return List.of();
        }
        return casePriorities;
    }

}
