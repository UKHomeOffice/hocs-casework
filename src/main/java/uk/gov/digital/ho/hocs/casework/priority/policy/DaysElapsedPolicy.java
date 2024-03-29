package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@AllArgsConstructor
@Getter
public class DaysElapsedPolicy implements StagePriorityPolicy {

    private String propertyName;

    private String propertyValue;

    private String dateFieldName;

    private String dateFormat;

    private int capNumberOfDays;

    private double capPointsToAward;

    private double pointsToAwardPerDay;

    @Override
    public double apply(StageWithCaseData stageWithCaseData) {
        var data = stageWithCaseData.getData();

        if (propertyValue.equals(data.get(propertyName))) {
            String dateString = data.get(dateFieldName);
            if (StringUtils.hasText(dateString)) {
                LocalDate now = LocalDate.now();
                LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));

                int daysElapsed = (int) ChronoUnit.DAYS.between(dateToCheck, now);
                if (capNumberOfDays > -1 && daysElapsed >= capNumberOfDays) {
                    return capPointsToAward;
                }
                return pointsToAwardPerDay * daysElapsed;
            }
        }
        return 0;
    }

}
