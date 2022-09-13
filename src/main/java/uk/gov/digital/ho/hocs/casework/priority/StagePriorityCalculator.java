package uk.gov.digital.ho.hocs.casework.priority;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

public interface StagePriorityCalculator {

    String SYSTEM_PRIORITY_FIELD_NAME = "systemCalculatedPriority";

    void updatePriority(StageWithCaseData stage, String caseType);

}
