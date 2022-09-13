package uk.gov.digital.ho.hocs.casework.priority.policy;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

public interface StagePriorityPolicy {

    double apply(StageWithCaseData data);

}
