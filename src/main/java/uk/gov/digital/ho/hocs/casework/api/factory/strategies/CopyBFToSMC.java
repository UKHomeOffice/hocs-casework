package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseDocumentService;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.api.factory.CaseCopy;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

@Service
@CaseCopy(fromCaseType = "BF", toCaseType = "SMC")
public class CopyBFToSMC extends AbstractCaseCopyStrategy implements CaseCopyStrategy {

    // For SMC these variables do not map to init vars
    private static final String[] DATA_CLOB_KEYS = { "Channel", "CompType", "PrevCompRef", "3rdPartyRef",
        "ComplainantDOB", "ComplainantHORef", "ComplainantPortRef", "ComplainantCompanyName", "ComplainantNationality",
        "ComplainantGender" };

    private static final String[] DOCUMENT_TYPES = { "To document", "Complaint leaflet", "Complaint letter",
        "Public correspondence", "Email", "CRF", "DRAFT" };

    private final CaseDataService caseDataService;

    private final CaseDocumentService caseDocumentService;

    private final CorrespondentService correspondentService;

    @Autowired
    public CopyBFToSMC(CaseDataService caseDataService,
                       CorrespondentService correspondentService,
                       CaseDocumentService caseDocumentService) {
        super();
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
        this.caseDocumentService = caseDocumentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        toCase.update("PreviousCaseReference", fromCase.getReference());
        fromCase.update("TransferToCaseRef", toCase.getReference());

        //Mapping to init vars
        String caseSumVal = fromCase.getData("CaseSummary");
        String ownCSUVal = fromCase.getData("OwningCSU");
        toCase.update("InitCaseSummary", caseSumVal!=null ? caseSumVal:"");
        toCase.update("InitOwningCSU", ownCSUVal!=null ? ownCSUVal:"");

        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());

        caseDocumentService.copyDocuments(fromCase.getUuid(), toCase.getUuid(), DOCUMENT_TYPES);

        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

}
