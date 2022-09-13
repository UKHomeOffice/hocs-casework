package uk.gov.digital.ho.hocs.casework.contributions;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Set;

public interface ContributionsProcessor {

    void processContributionsForStages(Set<StageWithCaseData> stage);

}
