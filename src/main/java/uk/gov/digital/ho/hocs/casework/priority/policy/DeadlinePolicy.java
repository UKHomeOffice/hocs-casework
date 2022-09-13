package uk.gov.digital.ho.hocs.casework.priority.policy;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.Duration;
import java.time.LocalDate;

public class DeadlinePolicy implements StagePriorityPolicy {

    @Override
    public double apply(StageWithCaseData stageWithCaseData) {
        var deadline = stageWithCaseData.getDeadline();

        return Duration.between(deadline.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays();
    }

}
