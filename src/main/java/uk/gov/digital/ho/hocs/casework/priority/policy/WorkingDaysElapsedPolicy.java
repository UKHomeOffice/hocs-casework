package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@AllArgsConstructor
@Getter
public class WorkingDaysElapsedPolicy implements StagePriorityPolicy {

    private WorkingDaysElapsedProvider workingDaysElapsedProvider;

    private String propertyName;
    private String propertyValue;
    private String dateFieldName;
    private String dateFormat;
    private int capNumberOfDays;
    private double capPointsToAward;
    private double pointsToAwardPerDay;

    @Override
    public double apply(Map<String, String> data) {
        if (propertyValue.equals(data.get(propertyName))) {
            String dateString = data.get(dateFieldName);
            if (StringUtils.hasText(dateString)) {
                LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));

                int daysElapsed = workingDaysElapsedProvider.getWorkingDaysSince(data.get(CASE_TYPE), dateToCheck);
                if (capNumberOfDays > -1 && daysElapsed >= capNumberOfDays) {
                    return capPointsToAward;
                }
                return pointsToAwardPerDay * daysElapsed;
            }
        }
        return 0;
    }
}
