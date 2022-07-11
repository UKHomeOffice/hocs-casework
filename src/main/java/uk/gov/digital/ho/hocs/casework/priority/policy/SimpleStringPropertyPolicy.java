package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SimpleStringPropertyPolicy implements StagePriorityPolicy {

    private String propertyName;
    private String propertyValue;
    private double pointsToAward;

    @Override
    public double apply(StageWithCaseData stageWithCaseData) {
        var data = stageWithCaseData.getData();

        if (propertyValue.equals(data.get(propertyName))) {
            return pointsToAward;
        }
        return 0;
    }
}
