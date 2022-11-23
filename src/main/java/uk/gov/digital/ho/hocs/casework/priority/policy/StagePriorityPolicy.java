package uk.gov.digital.ho.hocs.casework.priority.policy;

import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
public interface StagePriorityPolicy {

    double apply(CaseData caseData, ActiveStage stage);

}
