package uk.gov.digital.ho.hocs.casework.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
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
        primeNominatedPeopleCache();
    }

    @Scheduled(cron = "0 0/30 6-18 * * MON-FRI")
    private void primeCaseTypes(){
        try {
            this.infoClient.getCaseTypesByShortCodeRequest();
        } catch(Exception e) {
            log.warn("Failed to prime Teams. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "15 0/30 6-18 * * MON-FRI")
    private void primeSummaryFieldsCache(){
        try {
            Map<String, CaseDataType> caseTypesSet = this.infoClient.getCaseTypesByShortCodeRequest();
            for (CaseDataType caseType : caseTypesSet.values()) {
                this.infoClient.getCaseSummaryFieldsRequest(caseType.getDisplayCode());
            }
        } catch(Exception e) {
            log.warn("Failed to prime Summary Fields. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "30 0/30 6-18 * * MON-FRI")
    private void primeTeamCache(){
        try {
            this.infoClient.getTeamsRequest();
        } catch(Exception e) {
            log.warn("Failed to prime Teams. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "0 1/30 6-18 * * MON-FRI")
    private void primeStandardLinesCache(){
        try {
            this.infoClient.getStandardLinesByTopicUUIDRequest();
        } catch(Exception e) {
            log.warn("Failed to prime StandardLines. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "30 1/30 6-18 * * MON-FRI")
    private void primeTemplatesCache(){
        try {
            this.infoClient.getTemplatesByCaseTypeRequest();
        } catch(Exception e) {
            log.warn("Failed to prime Templates. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "0 55 5 * * MON-FRI")
    private void primeCaseDeadlineCache(){
        try {
            Map<String, CaseDataType> caseTypesSet = this.infoClient.getCaseTypesByShortCodeRequest();
            LocalDate now = LocalDate.now();
            for (CaseDataType caseType : caseTypesSet.values()) {
                try {
                    this.infoClient.getCaseDeadline(caseType.getDisplayCode(), now);
                } catch (Exception e) {
                    log.warn("Failed to prime Deadline {}", caseType.getDisplayCode(), value(EVENT, CACHE_PRIME_FAILED));
                }
                try {
                    this.infoClient.getStageDeadlines(caseType.getDisplayCode(), now);
                } catch (Exception e) {
                    log.warn("Failed to prime stage Deadline {}", caseType.getDisplayCode(), value(EVENT, CACHE_PRIME_FAILED));
                }
            }
        } catch(Exception e) {
            log.warn("Failed to prime Deadlines. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

    @Scheduled(cron = "0 0 5 * * MON-FRI")
    private void primeNominatedPeopleCache(){
        try {
            Set<TeamDto> teams = this.infoClient.getTeamsRequest();
            for (TeamDto teamDto : teams) {
                this.infoClient.getNominatedPeople(teamDto.getUuid());
            }
        } catch(Exception e) {
            log.warn("Failed to prime Nominated People. :" + e.toString(), value(EVENT, CACHE_PRIME_FAILED));
        }
    }

}