package uk.gov.digital.ho.hocs.casework.contributions;

import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.util.Set;

public interface ContributionsProcessor {

    void processContributionsForCase(CaseData caseData);

}
