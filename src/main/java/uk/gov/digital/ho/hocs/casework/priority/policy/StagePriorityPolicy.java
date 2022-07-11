package uk.gov.digital.ho.hocs.casework.priority.policy;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

public interface StagePriorityPolicy {
    String CASE_TYPE = "CASE_TYPE_MARKER";

    double apply(StageWithCaseData data);
}
