package uk.gov.digital.ho.hocs.casework.contributions;

import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

public interface ContributionsProcessor {
    void processContributionsForStage(Stage stage);
}
