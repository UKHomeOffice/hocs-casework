package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

@Service
@CaseCopy(fromCaseType = "BF", toCaseType = "BF2")
public class CopyBfToBf2 extends AbstractCaseCopyStrategy implements CaseCopyStrategy {

    private static final String[] DATA_CLOB_KEYS = {
            "Channel",
            "CaseSummary",
            "PrevCompRef",
            "3rdPartyRef",
            "CatDelay",
            "CatAdminErr",
            "CatPoorComm",
            "CatWrongInfo",
            "CatLost",
            "CatCCPhy",
            "CatCCAvail",
            "CatCCProvMinor",
            "CatCCHandle",
            "CatDamBF",
            "CatCustodyBF",
            "CatRude",
            "CatUnfair",
            "CatOtherUnprof",
            "CatTheft",
            "CatAssault",
            "CatSexAssault",
            "CatFraud",
            "CatRacism",
            "OwningCSU",
            "ComplainantDOB",
            "ComplainantHORef",
            "ComplainantPortRef",
            "ComplainantCompanyName",
            "ComplainantNationality",
            "ComplainantGender",
            "Region",
            "BusArea",
            "EnquiryReason1",
            "EnquiryReason2",
            "EnquiryReason3",
            "LoaRequired",
            "BusinessAreaOther"
    };

    private final CaseDataService caseDataService;
    private final CorrespondentService correspondentService;

    @Autowired
    public CopyBfToBf2(CaseDataService caseDataService, CorrespondentService correspondentService) {
        super();
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        // copy clob details
        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());

        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

}
