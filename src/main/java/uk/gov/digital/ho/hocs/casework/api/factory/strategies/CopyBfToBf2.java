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

    private static final String[] DATA_CLOB_KEYS = { "OwningCSU", "ComplainantDOB", "ComplainantHORef",
        "ComplainantPortRef", "ComplainantCompanyName", "ComplainantNationality", "Region",
        "BusArea", "ComplaintReason1", "ComplaintReason2", "ComplaintReason3", "ComplaintReason4", "ComplaintReason5",
        "ComplaintReason1_Other", "ComplaintReason2_Other", "ComplaintReason3_Other", "ComplaintReason4_Other",
        "ComplaintReason5_Other", "LoaRequired", "BusinessAreaOther" };

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
        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

        // copy clob details
        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        if (toCase.getPrimaryCorrespondentUUID()!=null) {
            toCase.update("Correspondents", toCase.getPrimaryCorrespondentUUID().toString());
        }
        toCase.update("PreviousCaseReference", fromCase.getReference());
        toCase.update("CompType", "Service");
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());

    }

}
