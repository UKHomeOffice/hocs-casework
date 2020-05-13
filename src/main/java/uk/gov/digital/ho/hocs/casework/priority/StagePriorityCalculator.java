package uk.gov.digital.ho.hocs.casework.priority;

import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

public interface StagePriorityCalculator {

    String SYSTEM_PRIORITY_FIELD_NAME = "systemCalculatedPriority";

    void updatePriority(Stage stage);
}
