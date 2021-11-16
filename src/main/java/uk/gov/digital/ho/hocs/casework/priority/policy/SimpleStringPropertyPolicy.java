package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
public class SimpleStringPropertyPolicy implements StagePriorityPolicy {

    private String propertyName;
    private String propertyValue;
    private double pointsToAward;

    @Override
    public double apply(Map<String, String> data, Set<LocalDate> exemptions) {

        if (propertyValue.equals(data.get(propertyName))) {
            return pointsToAward;
        }
        return 0;
    }
}
