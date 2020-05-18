package uk.gov.digital.ho.hocs.casework.priority.policy;

import java.util.Map;

public interface StagePriorityPolicy {

    String CASE_TYPE = "CASE_TYPE_MARKER";

    double apply(Map<String, String> data);
}
