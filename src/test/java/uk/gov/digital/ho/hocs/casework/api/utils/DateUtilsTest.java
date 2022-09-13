package uk.gov.digital.ho.hocs.casework.api.utils;

import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class DateUtilsTest {

    private final Set<LocalDate> nonWorkingDateFriday = Set.of(LocalDate.parse("2020-01-01"));

    private final Set<LocalDate> nonWorkingDateOther = Set.of(LocalDate.parse("2020-01-02"));

    private final Set<LocalDate> bankHolidays = Set.of(LocalDate.parse("2020-01-01"), LocalDate.parse("2020-04-10"),
        LocalDate.parse("2020-04-13"), LocalDate.parse("2020-05-08"), LocalDate.parse("2020-05-25"),
        LocalDate.parse("2020-08-31"), LocalDate.parse("2020-12-25"), LocalDate.parse("2020-12-28"));

    @Test
    public void isDateNonWorkingDay_dateNull() {
        assertThat(DateUtils.isDateNonWorkingDay(null, Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_exemptionsNull() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-01-01"), null)).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_mondayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-11"), Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_tuesdayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-12"), Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_wednesdayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-13"), Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_thursdayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-14"), Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_fridayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-15"), Collections.emptySet())).isFalse();
    }

    @Test
    public void isDateNonWorkingDay_saturdayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-16"), Collections.emptySet())).isTrue();
    }

    @Test
    public void isDateNonWorkingDay_sundayDate_emptyExemption() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-05-17"), Collections.emptySet())).isTrue();
    }

    @Test
    public void isDateNonWorkingDay_fridayDate_exempt() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-01-01"), nonWorkingDateFriday)).isTrue();
    }

    @Test
    public void isDateNonWorkingDay_fridayDate_exemptOther() {
        assertThat(DateUtils.isDateNonWorkingDay(LocalDate.parse("2020-01-01"), nonWorkingDateOther)).isFalse();
    }

    @Test
    public void addWorkingDays_fromWorkingDay() {
        // given
        String receivedDateString = "2020-08-17";
        String expectedExtendedDateString = "2020-09-15";

        LocalDate receivedDate = LocalDate.parse(receivedDateString);
        LocalDate expectedExtendedDate = LocalDate.parse(expectedExtendedDateString);

        // when
        final LocalDate result = DateUtils.addWorkingDays(receivedDate, 20, bankHolidays);

        // then
        assertThat(result).isEqualTo(expectedExtendedDate);
    }

    @Test
    public void addWorkingDays_fromNonWorkingDay() {
        // given
        String receivedDateString = "2020-01-04";
        String expectedExtendedDateString = "2020-02-03";

        LocalDate receivedDate = LocalDate.parse(receivedDateString);
        LocalDate expectedExtendedDate = LocalDate.parse(expectedExtendedDateString);

        // when
        final LocalDate result = DateUtils.addWorkingDays(receivedDate, 20, bankHolidays);

        // then
        assertThat(result).isEqualTo(expectedExtendedDate);
    }

    @Test
    public void calculateRemainingWorkingDays_fromWorkingDay() {
        // given
        String today = "2020-08-17";
        String deadline = "2020-09-13";

        LocalDate todayDate = LocalDate.parse(today);
        LocalDate deadlineDate = LocalDate.parse(deadline);

        // when
        final int result = DateUtils.calculateRemainingWorkingDays(todayDate, deadlineDate, bankHolidays);

        // then
        assertThat(result).isEqualTo(19);
    }

    @Test
    public void calculateRemainingWorkingDays_fromNonWorkingDay() {
        // given
        String today = "2020-01-04";
        String deadline = "2020-02-03";

        LocalDate todayDate = LocalDate.parse(today);
        LocalDate deadlineDate = LocalDate.parse(deadline);

        // when
        final int result = DateUtils.calculateRemainingWorkingDays(todayDate, deadlineDate, bankHolidays);

        // then
        assertThat(result).isEqualTo(21);
    }

    @Test
    public void calculateWorkingDaysElapsedSinceDate_fromWorkingDay() {
        // given
        String start = "2020-08-17";
        String today = "2020-09-13";

        LocalDate startDate = LocalDate.parse(start);
        LocalDate todayDate = LocalDate.parse(today);

        // when
        final int result = DateUtils.calculateWorkingDaysElapsedSinceDate(startDate, todayDate, bankHolidays);

        // then
        assertThat(result).isEqualTo(19);
    }

    @Test
    public void calculateWorkingDaysElapsedSinceDate_fromNonWorkingDay() {
        // given
        String start = "2020-01-04";
        String today = "2020-02-03";

        LocalDate startDate = LocalDate.parse(start);
        LocalDate todayDate = LocalDate.parse(today);

        // when
        final int result = DateUtils.calculateWorkingDaysElapsedSinceDate(startDate, todayDate, bankHolidays);

        // then
        assertThat(result).isEqualTo(20);
    }

}
