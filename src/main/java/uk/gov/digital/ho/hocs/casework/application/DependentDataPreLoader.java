package uk.gov.digital.ho.hocs.casework.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CACHE_PRIME_FAILED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
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
        primeCaches();
    }

    private void primeCaches() {
        try{
        Set<CaseDataType> caseTypesSet = this.infoClient.getCaseTypes();
        for (CaseDataType caseType : caseTypesSet) {
            this.infoClient.getCaseType(caseType.getShortCode());
            this.infoClient.getCaseSummaryFields(caseType.getType());
        }
        } catch(Exception e) {
            log.warn("Failed to prime cache.", value(EVENT, CACHE_PRIME_FAILED));
        }
    }
}