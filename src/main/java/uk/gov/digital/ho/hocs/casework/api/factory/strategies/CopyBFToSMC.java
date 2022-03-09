package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

@Service
@CaseCopy(fromCaseType = "BF", toCaseType = "SMC")
public class CopyBFToSMC extends AbstractCaseCopyStrategy implements CaseCopyStrategy {

    private static final String[] DATA_CLOB_KEYS = {
            "Channel",
            "CompType",
            "CaseSummary",
            "PrevCompRef",
            "3rdPartyRef",
            "OwningCSU",
            "ComplainantDOB",
            "ComplainantHORef",
            "ComplainantPortRef",
            "ComplainantCompanyName",
            "ComplainantNationality",
            "ComplainantGender"
    };

    private final CaseDataService caseDataService;
    private final CorrespondentService correspondentService;

    @Autowired
    public CopyBFToSMC(CaseDataService caseDataService, CorrespondentService correspondentService) {
        super();
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        // copy clob details
        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        toCase.update("PreviousCaseReference", fromCase.getReference());
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());

        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

}
