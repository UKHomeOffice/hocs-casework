package uk.gov.digital.ho.hocs.casework.priority.policy;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

public interface StagePriorityPolicy {

    String CASE_TYPE = "CASE_TYPE_MARKER";

    double apply(Map<String, String> data, Set<LocalDate> exemptions);
}
