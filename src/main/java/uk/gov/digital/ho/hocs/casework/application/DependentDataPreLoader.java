package uk.gov.digital.ho.hocs.casework.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.Set;

@Component
@Profile({"cache"})
public class DependentDataPreLoader {

    private InfoClient infoClient;

    @Autowired
    public DependentDataPreLoader(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        primeCaseTypes();
    }

    private void primeCaseTypes() {
        Set<CaseDataType> caseTypesSet = this.infoClient.getCaseTypes();
        for (CaseDataType caseType : caseTypesSet) {
            this.infoClient.getCaseType(caseType.getShortCode());
            this.infoClient.getCaseSummaryFields(caseType.getDisplayCode());
        }
    }
}