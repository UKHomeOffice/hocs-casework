package uk.gov.digital.ho.hocs.casework.priority;

import java.util.Map;

public interface StagePriorityCalculator {

    String SYSTEM_PRIORITY_FIELD_NAME = "systemCalculatedPriority";

    void updatePriority(Map<String, String> data, String caseType);
}
