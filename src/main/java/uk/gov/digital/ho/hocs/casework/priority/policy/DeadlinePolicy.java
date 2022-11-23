package uk.gov.digital.ho.hocs.casework.priority.policy;

import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.Duration;
import java.time.LocalDate;
public class DeadlinePolicy implements StagePriorityPolicy {

    @Override
    public double apply(CaseData caseData, ActiveStage stage) {
        var deadline = stage.getDeadline();
        return Duration.between(deadline.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
    }

}
