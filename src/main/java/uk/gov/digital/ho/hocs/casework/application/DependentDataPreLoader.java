package uk.gov.digital.ho.hocs.casework.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.Map;

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
            Map<String, CaseDataType> caseTypesSet = this.infoClient.getCaseTypesByShortCode();
            for (CaseDataType caseType : caseTypesSet.values()) {
                this.infoClient.getCaseSummaryFields(caseType.getDisplayCode());
            }
            this.infoClient.getStandardLinesByTopicUUID();
            this.infoClient.getTemplatesByCaseType();
            this.infoClient.getTeams();
        } catch(Exception e) {
            log.warn("Failed to prime cache.", value(EVENT, CACHE_PRIME_FAILED));
        }
    }
}