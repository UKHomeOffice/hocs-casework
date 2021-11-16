package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
public class JoinedStringPropertyPolicy implements StagePriorityPolicy {

    private String firstPropertyName;
    private String firstPropertyValue;
    private String secondPropertyName;
    private String secondPropertyValue;
    private double pointsToAward;

    @Override
    public double apply(Map<String, String> data, Set<LocalDate> exemptions) {

        if (firstPropertyValue.equals(data.get(firstPropertyName)) && secondPropertyValue.equals(data.get(secondPropertyName))) {
            return pointsToAward;
        }
        return 0;
    }
}
