package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_EXEMPTION_DATE_CACHE_ERROR;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.REPORT_MAPPER_EXEMPTION_DATE_CACHE_REFRESH;

@Slf4j
@Service
@Profile("reports")
public class ExemptionDatesAgeAdjustmentLookup {

    private final InfoClient infoServiceClient;

    private final Clock clock;

    private Map<String, TreeSet<LocalDate>> exceptionDatesByCaseType = Collections.emptyMap();


    @Autowired
    public ExemptionDatesAgeAdjustmentLookup(InfoClient infoServiceClient, Clock clock) {
        this.infoServiceClient = infoServiceClient;
        this.clock = clock;

        refreshCache();
    }

    private TreeSet<LocalDate> fetchExceptionDatesForCaseType(String type) {
        return new TreeSet<>(infoServiceClient.getExemptionDatesForType(type));
    }

    public void refreshCache() {
        log.info("Refreshing cache", value(EVENT, REPORT_MAPPER_EXEMPTION_DATE_CACHE_REFRESH));
        exceptionDatesByCaseType = infoServiceClient
            .getAllCaseTypes()
            .stream()
            .collect(Collectors.toMap(CaseDataType::getDisplayCode,
                type -> fetchExceptionDatesForCaseType(type.getDisplayCode())
                                     ));
    }

    public int getExemptionDatesForCaseTypeSince(String caseType, LocalDate fromDate) {
        return getExemptionDatesForCaseTypeBetween(caseType, fromDate, LocalDate.now(clock));
    }

    public int getExemptionDatesForCaseTypeBetween(String caseType, LocalDate fromDate, LocalDate toDate) {
        if (!exceptionDatesByCaseType.containsKey(caseType)) {

            log.warn("No cached exemption dates for case type {}", caseType,
                value(EVENT, REPORT_MAPPER_EXEMPTION_DATE_CACHE_ERROR)
                    );
        }

        return getExemptionDateSet(caseType)
            .subSet(fromDate, true, toDate, true)
            .size();
    }

    private TreeSet<LocalDate> getExemptionDateSet(String caseType) {
        return exceptionDatesByCaseType.computeIfAbsent(caseType, this::fetchExceptionDatesForCaseType);
    }

}
