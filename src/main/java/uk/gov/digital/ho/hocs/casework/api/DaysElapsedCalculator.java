package uk.gov.digital.ho.hocs.casework.api;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

public interface DaysElapsedCalculator {

    String SYSTEM_DAYS_ELAPSED_FIELD_NAME = "systemDaysElapsed";

    void updateDaysElapsed(StageWithCaseData stage);
}
