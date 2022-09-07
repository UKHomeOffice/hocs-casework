package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;
import uk.gov.digital.ho.hocs.casework.api.WorkingDaysElapsedProvider;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
    public double apply(StageWithCaseData stageWithCaseData) {
        var data = stageWithCaseData.getData();

        if (propertyName != null && propertyValue != null && !propertyValue.equals(data.get(propertyName))) {
            return 0;
        }

        String dateString = data.get(dateFieldName);
        if (StringUtils.hasText(dateString)) {
            LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));

            int daysElapsed = workingDaysElapsedProvider.getWorkingDaysSince(stageWithCaseData.getCaseDataType(),
                dateToCheck);
            if (capNumberOfDays > -1 && daysElapsed >= capNumberOfDays) {
                return capPointsToAward;
            }
            return pointsToAwardPerDay * daysElapsed;
        }

        return 0;
    }

}
