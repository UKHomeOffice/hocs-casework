package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

@AllArgsConstructor
@Getter
public class JoinedStringPropertyPolicy implements StagePriorityPolicy {

    private String firstPropertyName;

    private String firstPropertyValue;

    private String secondPropertyName;

    private String secondPropertyValue;

    private double pointsToAward;

    @Override
    public double apply(StageWithCaseData stageWithCaseData) {
        var data = stageWithCaseData.getData();

        if (firstPropertyValue.equals(data.get(firstPropertyName)) && secondPropertyValue.equals(
            data.get(secondPropertyName))) {
            return pointsToAward;
        }
        return 0;
    }

}
