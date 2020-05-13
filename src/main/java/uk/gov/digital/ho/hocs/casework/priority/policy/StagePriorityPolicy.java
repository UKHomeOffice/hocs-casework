package uk.gov.digital.ho.hocs.casework.priority.policy;

import java.util.Map;

public interface StagePriorityPolicy {

    double apply(Map<String, String> data);
}
