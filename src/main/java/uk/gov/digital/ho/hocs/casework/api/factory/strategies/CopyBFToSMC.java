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

    private static final String[] DATA_CLOB_KEYS = {
            "Channel",
            "CompType",
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

    private static final String[] DOCUMENT_TYPES = {
            "To document",
            "Complaint leaflet",
            "Complaint letter",
            "Public correspondence",
            "Email",
            "CRF",
            "DRAFT"
    };

    private final CaseDataService caseDataService;
    private final CaseDocumentService caseDocumentService;
    private final CorrespondentService correspondentService;

    @Autowired
    public CopyBFToSMC(CaseDataService caseDataService, CorrespondentService correspondentService, CaseDocumentService caseDocumentService) {
        super();
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
        this.caseDocumentService = caseDocumentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        // copy clob details
        copyClobData(fromCase, toCase, DATA_CLOB_KEYS);
        toCase.update("PreviousCaseReference", fromCase.getReference());
        toCase.update("InitCaseSummary", fromCase.getData("CaseSummary"));
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap());

        caseDocumentService.copyDocuments(fromCase.getUuid(), toCase.getUuid(), DOCUMENT_TYPES);


        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

}
