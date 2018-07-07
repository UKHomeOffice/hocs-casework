package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.CaseDataAudit;
import uk.gov.digital.ho.hocs.casework.audit.model.ExtractLine;
import uk.gov.digital.ho.hocs.casework.audit.model.StageDataAudit;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.UnitType;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CaseDataAuditService {

    private static Map<UnitType, CaseType[]> unitToCaseTypesMapping;
    private static Map<UnitType, String> unitToReportHeadingMapping;

    static {
        unitToCaseTypesMapping = new EnumMap<>(UnitType.class);
        unitToCaseTypesMapping.put(UnitType.RSH, new CaseType[]{CaseType.RSH});
        unitToCaseTypesMapping.put(UnitType.DCU, new CaseType[]{CaseType.MIN, CaseType.TRO, CaseType.DTEN});
        unitToCaseTypesMapping.put(UnitType.UKVI, new CaseType[]{CaseType.IMCB, CaseType.IMCM, CaseType.UTEN});
        unitToCaseTypesMapping.put(UnitType.FOI, new CaseType[]{CaseType.FOI, CaseType.FTC, CaseType.FTCI, CaseType.FSC, CaseType.FSCI});
        unitToCaseTypesMapping.put(UnitType.HMPOCOR, new CaseType[]{CaseType.COM, CaseType.COM1, CaseType.COM2, CaseType.DGEN, CaseType.GNR});
        unitToCaseTypesMapping.put(UnitType.HMPOCOL, new CaseType[]{CaseType.COL});

        unitToReportHeadingMapping = new EnumMap<>(UnitType.class);
        unitToReportHeadingMapping.put(UnitType.RSH, "Case_Type,Case_Reference,Case_UUID,Case_Timestamp,Stage_legacy-reference,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Timestamp,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email");
    }

    private final AuditService auditService;
    private final CaseDataAuditRepository caseDataAuditRepository;
    private final StageDataAuditRepository stageDataAuditRepository;
    private final ObjectMapper objectMapper;
    private final RequestData requestData;

    @Autowired
    public CaseDataAuditService(AuditService auditService, CaseDataAuditRepository caseDataAuditRepository, StageDataAuditRepository stageDataAuditRepository, RequestData requestData) {
        this.auditService = auditService;
        this.caseDataAuditRepository = caseDataAuditRepository;
        this.stageDataAuditRepository = stageDataAuditRepository;
        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
        this.requestData = requestData;
    }

    String getReportingDataAsCSV(UnitType unit, LocalDate cutoff) {
        String heading = unitToReportHeadingMapping.get(unit);

        if (heading != null && cutoff != null) {
            auditService.writeExtractEvent(String.join(" ", unit.toString(), cutoff.toString()));

            // Get the reporting data
            List<ExtractLine> extractLines = getReportingData(unit, heading, cutoff);

            // Turn it into a CSV
            return createCSV(heading, extractLines);
        } else {
            return "";
        }
    }

    private List<ExtractLine> getReportingData(UnitType unit, String header, LocalDate cutoff) {
        log.info("Starting Extract, Unit: \"{}\",  User: {}", unit, cutoff, requestData.username());

        // Start date, End data and a string List of case types.
        LocalDateTime startDate = getStartDate(cutoff);
        LocalDateTime endDate = getEndDate(cutoff);
        List<String> types = Arrays.stream(unitToCaseTypesMapping.get(unit)).map(Enum::toString).collect(Collectors.toList());

        // Get the latest version of each case and stage in the system e.g. select max(id) group by case/stage uuid.
        Set<CaseDataAudit> caseDataAudits = caseDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<StageDataAudit> stageDataAudits = stageDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        // Group the stages that have the same caseUUID.
        Map<UUID, List<StageDataAudit>> groupedStages = stageDataAudits.stream().collect(Collectors.groupingBy(StageDataAudit::getCaseUUID));

        // Turn each Case and related Stages into one long report line each.
        List<ExtractLine> extractLines = caseDataAudits.stream().map(caseDataAudit -> getExtractLine(caseDataAudit, groupedStages.get(caseDataAudit.getUuid()), header, objectMapper)).collect(Collectors.toList());

        log.info("Returned Extract, Found: {}, User: {}", caseDataAudits.size(), requestData.username());

        return extractLines;
    }

    private static ExtractLine getExtractLine(CaseDataAudit caseDataAudit, List<StageDataAudit> stages, String header, ObjectMapper objectMapper) {
        // Turn the case into a Map of (Heading, Value).
        Map<String, String> lineData = caseToMap(caseDataAudit);

        // If there are any stages do the same to those.
        if (stages != null && !stages.isEmpty()) {
            stages.forEach(stageDataAudit -> lineData.putAll(stageToMap(stageDataAudit, objectMapper)));
        }

        return ExtractLine.from(header, lineData);
    }

    private static Map<String, String> caseToMap(CaseDataAudit caseDataAudit) {
        Map<String, String> caseMap = new HashMap<>();

        String caseName = "Case";
        caseMap.put(columnNameFormat(caseName, "UUID"), caseDataAudit.getUuid().toString());
        caseMap.put(columnNameFormat(caseName, "Type"), caseDataAudit.getType());
        caseMap.put(columnNameFormat(caseName, "Timestamp"), caseDataAudit.getTimestamp().toString());
        caseMap.put(columnNameFormat(caseName, "Reference"), caseDataAudit.getReference());

        return caseMap;
    }


    private static Map<String, String> stageToMap(StageDataAudit stageDataAudit, ObjectMapper objectMapper) {
        Map<String, String> stageMap = new HashMap<>();

        String stageName = stageDataAudit.getType();
        stageMap.put(columnNameFormat(stageName, "UUID"), stageDataAudit.getUuid().toString());
        stageMap.put(columnNameFormat(stageName, "Type"), stageDataAudit.getType());
        stageMap.put(columnNameFormat(stageName, "Timestamp"), stageDataAudit.getTimestamp().toString());
        stageMap.put(columnNameFormat(stageName, "CaseUUID"), stageDataAudit.getCaseUUID().toString());

        try {
            Map<String, String> dataMap = objectMapper.readValue(stageDataAudit.getData(), new TypeReference<HashMap<String, String>>() {});

            // We can't use putAll here because we want to change the Key name
            dataMap.forEach((key, value) -> stageMap.put(columnNameFormat(stageName, key), value));
        } catch (IOException e) {
            log.error(e.toString());
        }

        return stageMap;
    }

    private static String createCSV(String header, List<ExtractLine> extractLines) {
        StringBuilder sb = new StringBuilder();
        try(CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT)) {
            printer.printRecord(header);
            extractLines.forEach(l -> printExportLine(printer, l));
        } catch (IOException e) {
            log.warn(e.toString());
        }

        return sb.toString();
    }

    private static void printExportLine(CSVPrinter printer, ExtractLine l) {
        try {
            printer.printRecord(l.getLineData().values());
        } catch (IOException e) {
            log.warn(e.toString());
        }
    }

    private static LocalDateTime getStartDate(LocalDate cutoff) {
        int monthsBack = 4;
        // Start at the first day of the month
        return LocalDateTime.of(cutoff.minusMonths(monthsBack).getYear(), cutoff.minusMonths(monthsBack).getMonth(), 1, 0, 0);
    }

    private static LocalDateTime getEndDate(LocalDate cutoff) {
        return LocalDateTime.of(cutoff, LocalTime.MAX);
    }

    private static String columnNameFormat(String prefix, String suffix) {
        return String.format("%s_%s", prefix, suffix);
    }
}
