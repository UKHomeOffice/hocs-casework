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
@CaseCopy(fromCaseType = "COMP", toCaseType = "COMP2")
public class CopyCompToComp2 implements CaseCopyStrategy {

    private ObjectMapper mapper;
    private CaseDataService caseDataService;
    private CorrespondentService correspondentService;

    @Autowired
    public CopyCompToComp2(ObjectMapper mapper, CaseDataService caseDataService, CorrespondentService correspondentService) {
        this.mapper = mapper;
        this.caseDataService = caseDataService;
        this.correspondentService = correspondentService;
    }

    @Override
    public void copyCase(CaseData fromCase, CaseData toCase) {

        // copy clob details
        copyClobData(fromCase, toCase);
        caseDataService.updateCaseData(toCase.getUuid(), null, toCase.getDataMap(mapper));

        // Correspondents include the primary_correspondent_uuid
        correspondentService.copyCorrespondents(fromCase.getUuid(), toCase.getUuid());

    }

    private void copyClobData(CaseData fromCase, CaseData toCase) {
        String[] dataClobKeys = new String[]{
                "CatLost",
                "CatRude",
                "Channel",
                "CatCCPhy",
                "CatDamBF",
                "CatDelay",
                "CatFraud",
                "CatTheft",
                "CompType",
                "Severity",
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
                "PrevUkviRef",
                "CatCustodyBF",
                "CatWrongInfo",
                "CatSexAssault",
                "CatCCProvMinor",
                "CatOtherUnprof",
                "ComplainantDOB",
                "ComplainantHORef",
                "ComplainantGender",
                "ComplainantPortRef",
                "SeverityVulnerable",
                "SeveritySafeGuarding",
                "ComplainantCompanyName",
                "ComplainantNationality"
        };

        Map<String, String> fromCaseClobData = fromCase.getDataMap(mapper);
        Map<String, String> toCaseClobData = toCase.getDataMap(mapper);

        for (String dataClobKey : dataClobKeys) {
            toCaseClobData.put(dataClobKey, fromCaseClobData.get(dataClobKey));
        }

        toCase.update(toCaseClobData, mapper);
    }

}
