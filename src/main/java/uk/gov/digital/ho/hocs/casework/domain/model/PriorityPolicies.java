package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class PriorityPolicies {

    @JsonValue
    private final Map<String, List<PriorityPolicy>> casePolicies;

    @JsonCreator
    public PriorityPolicies(Map<String, List<PriorityPolicy>> casePolicies) {
        this.casePolicies = casePolicies;
    }

    public List<PriorityPolicy> getPoliciesForCaseType(String caseType) {
        return casePolicies.get(caseType);
    }

    @Getter
    public static class PriorityPolicy {

        private final String type;
        private final Map<String, String> config;

        @JsonCreator
        public PriorityPolicy(@JsonProperty("type") String type, @JsonProperty("config") Map<String, String> config) {
            this.type = type;
            this.config = config;
        }

    }
}
