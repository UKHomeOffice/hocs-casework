package uk.gov.digital.ho.hocs.casework.api.utils;


import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public final class DateUtils {

    private static boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    public static boolean isDateNonWorkingDay(LocalDate date, Set<LocalDate> holidayDates) {
        if (date == null || holidayDates == null) {
            return false;
        }

        return (DateUtils.isWeekend(date) || holidayDates.contains(date));
    }

    /**
     * Returns the date after a number of working days after the startDate. If the startDate, is not a working day
     * the calculation begins from the next first working day.
     *
     * Non-working days are considered as weekends and dates in the holidays set
     *
     * @param startDate the start date
     * @param workingDays the number of days to add
     * @param holidays a set of days to be considered as non-working
     * @return
     */
    public static LocalDate addWorkingDays(LocalDate startDate, int workingDays, Set<LocalDate> holidays) {
        LocalDate result = startDate;

        // Start from the next first working day
        while (DateUtils.isDateNonWorkingDay(result, holidays)) {
            result = result.plusDays(1);
        }

        int i = 1;
        while (i <= workingDays) {
            result = result.plusDays(1);
            // Only increment Mon-Fri and non-holidays
            if (!(isDateNonWorkingDay(result, holidays))) {
                ++i;
            }
        }
        return result;
    }
}
