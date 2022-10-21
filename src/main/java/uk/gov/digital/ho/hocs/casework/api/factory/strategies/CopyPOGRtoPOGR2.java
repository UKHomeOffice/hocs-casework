package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;

@Service
@CaseCopy(fromCaseType = "POGR", toCaseType = "POGR2")
public class CopyPOGRtoPOGR2 extends AbstractCaseCopyStrategy implements CaseCopyStrategy {

    private static final String[] DATA_CLOB_KEYS = { "BusinessArea", "ComplainantDOB", "ComplainantGender",
        "ComplainantNationality", "ComplainantCompanyName", "ComplainantCaseAccountNumber", "ComplainantCategory",
        "ComplainantNro", "ComplainantLocation", "ComplainantApplicationReference", "ComplainantPassportNumber",
        "ComplaintDescription", "ComplaintChannel", "ComplaintNationOrigin", "ComplaintPriority",
        "ComplaintThirdPartyReference", "ComplaintReason", "LoaRequired", "LoaReceived", "LoaDateReceived" };

    private final CaseDataService caseDataService;

    private final CorrespondentService correspondentService;

    @Autowired
    public CopyPOGRtoPOGR2(CaseDataService caseDataService, CorrespondentService correspondentService) {
        super();
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        toCase.update("Correspondents", toCase.getPrimaryCorrespondentUUID().toString());
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());
    }

}
