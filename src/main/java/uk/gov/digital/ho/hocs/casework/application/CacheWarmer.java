package uk.gov.digital.ho.hocs.casework.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStandardLineResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTemplateResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CACHE_PRIME_FAILED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Component
@Profile({"cache"})
public class CacheWarmer {

    private InfoClient infoClient;

    @Autowired
    public CacheWarmer(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    @EventListener
    public void onApplicationEvent(ApplicationReadyEvent event) {
        primeCaches();
    }

    private void primeCaches() {
        primeCaseTypes();
        primeStandardLinesCache();
        primeTemplatesCache();
        primeSummaryFieldsCache();
        primeTeamCache();
        primeCaseDeadlineCache();
    }

    @Scheduled(cron = "0 0/30 6-18 * * MON-FRI")
    private void primeCaseTypes(){
        try {
            Set<CaseDataType> caseDataTypeSet = this.infoClient.getCaseTypes();
            for(CaseDataType caseDataType : caseDataTypeSet) {
                this.infoClient.populateCaseTypeByShortCode(caseDataType.getShortCode(), caseDataType);
                this.infoClient.populateCaseType(caseDataType.getDisplayCode(), caseDataType);
            }
        } catch(Exception e) {
            log.warn("Failed to prime Teams. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "5 1/30 6-18 * * MON-FRI")
    private void primeStandardLinesCache(){
        try {
            Set<GetStandardLineResponse> standardLineResponses = this.infoClient.getStandardLinesByTopicUUIDRequest();
            for(GetStandardLineResponse getStandardLineResponse : standardLineResponses) {
                this.infoClient.populateStandardLine(getStandardLineResponse.getTopicUUID(), getStandardLineResponse);
            }
        } catch(Exception e) {
            log.warn("Failed to prime StandardLines. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "10 1/30 6-18 * * MON-FRI")
    private void primeTemplatesCache(){
        try {
            Set<GetTemplateResponse> templateResponses = this.infoClient.getTemplatesByCaseTypeRequest();
            for(GetTemplateResponse getTemplateResponse : templateResponses) {
                this.infoClient.populateTemplate(getTemplateResponse.getCaseType(), getTemplateResponse);
            }
        } catch(Exception e) {
            log.warn("Failed to prime Templates. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "15 0/30 6-18 * * MON-FRI")
    private void primeTeamCache(){
        try {
            this.infoClient.populateTeams();
        } catch(Exception e) {
            log.warn("Failed to prime Teams. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "20 0/30 6-18 * * MON-FRI")
    private void primeSummaryFieldsCache(){
        try {
           Set<CaseDataType> caseTypesSet = this.infoClient.getCaseTypes();
            for (CaseDataType caseType : caseTypesSet) {
                this.infoClient.populateCaseSummaryFields(caseType.getDisplayCode());
            }
        } catch(Exception e) {
            log.warn("Failed to prime Summary Fields. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "0 55 5 * * MON-FRI")
    private void primeCaseDeadlineCache(){
        try {
           Set<CaseDataType> caseTypesSet = this.infoClient.getCaseTypes();
            LocalDate now = LocalDate.now();
            for (CaseDataType caseType : caseTypesSet) {
                try {
                    this.infoClient.getCaseDeadline(caseType.getDisplayCode(), now);
                } catch (Exception e) {
                    log.warn("Failed to prime Deadline {}", caseType.getDisplayCode(), value(EVENT, CACHE_PRIME_FAILED));
                }
                try {
                    Map<String, LocalDate> stageDeadlines = this.infoClient.getStageDeadlines(caseType.getDisplayCode(), now);
                    stageDeadlines.forEach((ct, d) -> this.infoClient.populateStageDeadline(ct, now, d));
                } catch (Exception e) {
                    log.warn("Failed to prime stage Deadline {}", caseType.getDisplayCode(), value(EVENT, CACHE_PRIME_FAILED));
                }
            }
        } catch(Exception e) {
            log.warn("Failed to prime Deadlines. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

}