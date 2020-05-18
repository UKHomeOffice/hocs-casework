package uk.gov.digital.ho.hocs.casework.priority;

import java.time.LocalDate;

public interface WorkingDaysElapsedProvider {

    Integer getWorkingDaysSince(String caseType, LocalDate fromDate);
}
