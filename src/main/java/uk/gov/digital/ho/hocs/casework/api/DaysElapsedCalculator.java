package uk.gov.digital.ho.hocs.casework.api;

import java.util.Map;

public interface DaysElapsedCalculator {

    String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";

    void updateDaysElapsed(Map<String, String> data, String caseType);

}
