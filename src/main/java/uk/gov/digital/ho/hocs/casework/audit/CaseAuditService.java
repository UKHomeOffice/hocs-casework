package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.CaseAuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.model.ReportLine;
import uk.gov.digital.ho.hocs.casework.audit.model.StageAuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.repository.CaseAuditRepository;
import uk.gov.digital.ho.hocs.casework.audit.repository.StageAuditRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.UnitType;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
class CaseAuditService {

    // TODO: move into info service.
    private static Map<UnitType, CaseDataType[]> unitToCaseTypesMapping;
    private static Map<UnitType, String> unitToReportHeadingMapping;

    private static int monthsBack = 4;
    static {
        unitToCaseTypesMapping = new EnumMap<>(UnitType.class);
        unitToCaseTypesMapping.put(UnitType.RSH, new CaseDataType[]{CaseDataType.RSH});
        unitToCaseTypesMapping.put(UnitType.DCU, new CaseDataType[]{CaseDataType.MIN});//, CaseDataType.TRO, CaseDataType.DTEN});
        //unitToCaseTypesMapping.put(UnitType.UKVI, new CaseDataType[]{CaseDataType.IMCB, CaseDataType.IMCM, CaseDataType.UTEN});
        //unitToCaseTypesMapping.put(UnitType.FOI, new CaseDataType[]{CaseDataType.FOI, CaseDataType.FTC, CaseDataType.FTCI, CaseDataType.FSC, CaseDataType.FSCI});
        //unitToCaseTypesMapping.put(UnitType.HMPOCOR, new CaseDataType[]{CaseDataType.COM, CaseDataType.COM1, CaseDataType.COM2, CaseDataType.DGEN, CaseDataType.GNR});
        //unitToCaseTypesMapping.put(UnitType.HMPOCOL, new CaseDataType[]{CaseDataType.COL});

        unitToReportHeadingMapping = new EnumMap<>(UnitType.class);
        unitToReportHeadingMapping.put(UnitType.RSH, "Case_Type,Case_Reference,Case_UUID,Case_Timestamp,Stage_legacy-reference,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Timestamp,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email");
    }

    private final AuditService auditService;
    private final CaseAuditRepository caseAuditRepository;
    private final StageAuditRepository stageAuditRepository;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;

    @Autowired
    public CaseAuditService(AuditService auditService, CaseAuditRepository caseAuditRepository, StageAuditRepository stageAuditRepository, RequestData requestData, ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.caseAuditRepository = caseAuditRepository;
        this.stageAuditRepository = stageAuditRepository;
        this.objectMapper = objectMapper;
        this.requestData = requestData;
    }

    private static ReportLine getExtractLine(CaseAuditEntry caseAuditEntry, List<StageAuditEntry> stages, String header, ObjectMapper objectMapper) {
        // Turn the case into a Map of (Heading, Value).
        Map<String, String> lineData = caseToMap(caseAuditEntry);

        // If there are any stages do the same to those.
        if (stages != null && !stages.isEmpty()) {
            stages.forEach(stageAuditEntry -> lineData.putAll(stageToMap(stageAuditEntry, objectMapper)));
        }

        return ReportLine.from(header, lineData);
    }

    private static Map<String, String> caseToMap(CaseAuditEntry caseAuditEntry) {
        Map<String, String> caseMap = new HashMap<>();

        String caseName = "Case";
        caseMap.put(columnNameFormat(caseName, "UUID"), caseAuditEntry.getUuid().toString());
        caseMap.put(columnNameFormat(caseName, "Type"), caseAuditEntry.getType());
        caseMap.put(columnNameFormat(caseName, "Timestamp"), caseAuditEntry.getTimestamp().toString());
        //caseMap.put(columnNameFormat(caseName, "Reference"), caseAuditEntry.getReference());

        return caseMap;
    }

    private static Map<String, String> stageToMap(StageAuditEntry stageAuditEntry, ObjectMapper objectMapper) {
        Map<String, String> stageMap = new HashMap<>();

        String stageName = stageAuditEntry.getType();
        stageMap.put(columnNameFormat(stageName, "UUID"), stageAuditEntry.getUuid().toString());
        stageMap.put(columnNameFormat(stageName, "Type"), stageAuditEntry.getType());
        stageMap.put(columnNameFormat(stageName, "Timestamp"), stageAuditEntry.getTimestamp().toString());
        stageMap.put(columnNameFormat(stageName, "CaseUUID"), stageAuditEntry.getCaseUUID().toString());

        //try {
        // Map<String, String> dataMap = objectMapper.readValue(stageAuditEntry.getData(), new TypeReference<HashMap<String, String>>() {
        // });

            // We can't use putAll here because we want to change the Key name
        // dataMap.forEach((key, value) -> stageMap.put(columnNameFormat(stageName, key), value));
        //} catch (IOException e) {
        //    log.error(e.toString());
        // }

        return stageMap;
    }

    private static String createCSV(String header, List<ReportLine> reportLines) {
        StringBuilder sb = new StringBuilder();
        try (CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT)) {
            printer.printRecord((Object[]) header.split(","));
            reportLines.forEach(l -> printExportLine(printer, l));
        } catch (IOException e) {
            log.warn(e.toString());
        }

        return sb.toString();
    }

    private static void printExportLine(CSVPrinter printer, ReportLine l) {
        try {
            printer.printRecord(l.getLineData().values());
        } catch (IOException e) {
            log.warn(e.toString());
        }
    }

    private static LocalDateTime getStartDate(LocalDate cutoff) {

        // Start at the first day of the month
        return LocalDateTime.of(cutoff.minusMonths(monthsBack).getYear(), cutoff.minusMonths(monthsBack).getMonth(), 1, 0, 0);
    }

    private static LocalDateTime getEndDate(LocalDate cutoff) {
        return LocalDateTime.of(cutoff, LocalTime.MAX);
    }

    private static String columnNameFormat(String prefix, String suffix) {
        return String.format("%s_%s", prefix, suffix);
    }

    private static boolean checkValidDateRange(LocalDate cutoff) {
        try {
            cutoff.minusMonths(monthsBack);
        } catch (DateTimeException e) {
            log.warn(e.toString());
            return false;
        }
        return true;
    }

    String getReportingDataAsCSV(UnitType unit, LocalDate cutoff) {

        String heading = unitToReportHeadingMapping.get(unit);

        if (heading != null && cutoff != null && checkValidDateRange(cutoff)) {
            auditService.extractReportEvent(String.join(" ", unit.toString(), cutoff.toString()));

            // Get the reporting data
            List<ReportLine> reportLines = getReportingData(unit, heading, cutoff);

            // Turn it into a CSV
            return createCSV(heading, reportLines);
        } else {
            return "";
        }
    }

    private List<ReportLine> getReportingData(UnitType unit, String header, LocalDate cutoff) {
        log.info("Starting Extract, Unit: \"{}\",  User: {}", unit, cutoff, requestData.username());

        // Start date, End data and a string List of case types.
        LocalDateTime startDate = getStartDate(cutoff);
        LocalDateTime endDate = getEndDate(cutoff);
        List<String> types = Arrays.stream(unitToCaseTypesMapping.get(unit)).map(Enum::toString).collect(Collectors.toList());

        // Get the latest version of each case and stage in the system e.g. select max(id) group by case/stage uuid.
        Set<CaseAuditEntry> auditCaseDataEntries = caseAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<StageAuditEntry> auditStageDataEntries = stageAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        // Group the stages that have the same caseUUID.
        Map<UUID, List<StageAuditEntry>> groupedStages = auditStageDataEntries.stream().collect(Collectors.groupingBy(StageAuditEntry::getCaseUUID));

        // Turn each Case and related Stages into one long report line each.
        List<ReportLine> reportLines = auditCaseDataEntries.stream().map(caseAuditEntry -> getExtractLine(caseAuditEntry, groupedStages.get(caseAuditEntry.getUuid()), header, objectMapper)).collect(Collectors.toList());

        log.info("Returned Extract, Found: {}, User: {}", auditCaseDataEntries.size(), requestData.username());

        return reportLines;
    }
}
