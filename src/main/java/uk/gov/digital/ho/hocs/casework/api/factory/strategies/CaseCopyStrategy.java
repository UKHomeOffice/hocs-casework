package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

public interface CaseCopyStrategy {

    void copyCase(CaseData fromCase, CaseData toCase);

}
