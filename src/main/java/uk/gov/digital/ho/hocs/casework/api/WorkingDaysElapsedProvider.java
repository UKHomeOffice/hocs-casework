package uk.gov.digital.ho.hocs.casework.api;

import java.time.LocalDate;

public interface WorkingDaysElapsedProvider {

    Integer getWorkingDaysSince(String caseType, LocalDate fromDate);

}
