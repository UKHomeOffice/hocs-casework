package uk.gov.digital.ho.hocs.casework.priority.policy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;

@AllArgsConstructor
@Getter
public class WorkingDaysElapsedPolicy implements StagePriorityPolicy {

    private String propertyName;
    private String propertyValue;
    private String dateFieldName;
    private String dateFormat;
    private int capNumberOfDays;
    private double capPointsToAward;
    private double pointsToAwardPerDay;


    @Override
    public double apply(Map<String, String> data, Set<LocalDate> exemptions) {
        if (propertyValue.equals(data.get(propertyName))) {
            String dateString = data.get(dateFieldName);
            if (StringUtils.hasText(dateString)) {
                LocalDate dateToCheck = LocalDate.parse(dateString, DateTimeFormatter.ofPattern(dateFormat));

                int daysElapsed = calculateWorkingDaysElapsedForCaseType(dateToCheck, exemptions);
                if (capNumberOfDays > -1 && daysElapsed >= capNumberOfDays) {
                    return capPointsToAward;
                }
                return pointsToAwardPerDay * daysElapsed;
            }
        }
        return 0;
    }

    public static int calculateWorkingDaysElapsedForCaseType(LocalDate fromDate, Set<LocalDate> exemptions) {

        LocalDate now = LocalDate.now();
        if (fromDate == null || now.isBefore(fromDate) || now.isEqual(fromDate)) {
            return 0;
        }
        LocalDate date = fromDate;
        int workingDays = 0;
        while (date.isBefore(now)) {
            if (!isDateNonWorkingDay(date, exemptions)) {
                workingDays++;
            }

            date = date.plusDays(1);
        }

        return workingDays;

    }

    private static boolean isDateNonWorkingDay(LocalDate date, Set<LocalDate> holidayDates) {
        if (date == null || holidayDates == null) {
            return false;
        }

        return (isWeekend(date) || holidayDates.contains(date));
    }

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
