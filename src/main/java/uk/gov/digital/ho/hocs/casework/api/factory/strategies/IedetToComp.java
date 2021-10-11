package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;

@Service
@CaseCopy(fromCaseType = "IEDET", toCaseType = "COMP")
public class IedetToComp extends AbstractCaseCopyStrategy implements CaseCopyStrategy {

    private static final String[] DATA_CLOB_KEYS = {
            "CatLost",
            "CatRude",
            "Channel",
            "CatCCPhy",
            "CatDamBF",
            "CatDelay",
            "CatFraud",
            "CatTheft",
            "CompType",
            "CatRacism",
            "CatUnfair",
            "OwningCSU",
            "CatAssault",
            "CatCCAvail",
            "3rdPartyRef",
            "CaseSummary",
            "CatAdminErr",
            "CatCCHandle",
            "CatPoorComm",
            "CatCustodyBF",
            "CatWrongInfo",
            "CatSexAssault",
            "CatCCProvMinor",
            "CatOtherUnprof",
            "ComplainantDOB",
            "ComplainantHORef",
            "ComplainantGender",
            "ComplainantPortRef",
            "ComplainantCompanyName",
            "ComplainantNationality"
    };

    private CaseDataService caseDataService;
    private CorrespondentService correspondentService;

    @Autowired
    public IedetToComp(ObjectMapper mapper, CaseDataService caseDataService, CorrespondentService correspondentService) {
        super(mapper);
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        // copy clob details
        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        Map<String, String> toCaseClobData = toCase.getDataMap(mapper);
        toCaseClobData.put("CurrentStage", "COMP_CCH_RETURNS");
        caseDataService.updateCaseData(toCase.getUuid(), null, toCaseClobData);

        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

}
