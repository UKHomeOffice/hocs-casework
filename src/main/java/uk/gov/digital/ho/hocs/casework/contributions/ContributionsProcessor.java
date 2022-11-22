package uk.gov.digital.ho.hocs.casework.contributions;

import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Collection;

public interface ContributionsProcessor {

    void processContributionsForStages(Collection<StageWithCaseData> stage);

}
