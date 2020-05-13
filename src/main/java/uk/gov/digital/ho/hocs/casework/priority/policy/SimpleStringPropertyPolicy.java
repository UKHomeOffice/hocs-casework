package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SimpleStringPropertyPolicy implements StagePriorityPolicy {

    private String propertyName;
    private String propertyValue;
    private double pointsToAward;

    @Override
    public double apply(Map<String, String> data) {

        if (propertyValue.equals(data.get(propertyName))) {
            return pointsToAward;
        }
        return 0;
    }
}
