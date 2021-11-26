package uk.gov.digital.ho.hocs.casework.api.factory;


import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.DcuMinCaseSummaryAdditionalFieldProvider;
import uk.gov.digital.ho.hocs.casework.api.factory.strategies.DefaultCaseSummaryAdditionalFieldProvider;

@Service
public class CaseSummaryAdditionalFieldProviderFactory {
    public CaseSummaryAdditionalFieldProvider getCaseSummaryAdditionalFieldProvider(String caseType) {
        switch(caseType) {
            // DCU
            case "MIN": return new DcuMinCaseSummaryAdditionalFieldProvider();
            default: return new DefaultCaseSummaryAdditionalFieldProvider();
        }
    }
}
