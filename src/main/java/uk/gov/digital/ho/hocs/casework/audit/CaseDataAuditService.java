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
import uk.gov.digital.ho.hocs.casework.audit.model.ExportLine;
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

    private static Map<UnitType, CaseType[]> caseTypesMapping;
    private static Map<UnitType, String> caseSchemaMapping;

    static {
        caseTypesMapping = new EnumMap<>(UnitType.class);
        caseTypesMapping.put(UnitType.RSH, new CaseType[]{CaseType.RSH});
        caseTypesMapping.put(UnitType.DCU, new CaseType[]{CaseType.MIN, CaseType.TRO, CaseType.DTEN});
        caseTypesMapping.put(UnitType.UKVI, new CaseType[]{CaseType.IMCB, CaseType.IMCM, CaseType.UTEN});
        caseTypesMapping.put(UnitType.FOI, new CaseType[]{CaseType.FOI, CaseType.FTC, CaseType.FTCI, CaseType.FSC, CaseType.FSCI});
        caseTypesMapping.put(UnitType.HMPOCOR, new CaseType[]{CaseType.COM, CaseType.COM1, CaseType.COM2, CaseType.DGEN, CaseType.GNR});
        caseTypesMapping.put(UnitType.HMPOCOL, new CaseType[]{CaseType.COL});

        caseSchemaMapping = new EnumMap<>(UnitType.class);
        caseSchemaMapping.put(UnitType.RSH, "Case_Type,Case_Reference,Case_UUID,Case_Timestamp,Stage_legacy-reference,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Timestamp,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email");
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
        String header = caseSchemaMapping.get(unit);

        if (header != null && cutoff != null) {
            auditService.writeExtractEvent(String.join(" ", unit.toString(), cutoff.toString()));

            List<ExportLine> exportLines = getReportingData(unit, header, cutoff);

            return getReportingDataCSV(header, exportLines);
        } else {
            return "";
        }
    }

    private List<ExportLine> getReportingData(UnitType unit, String header, LocalDate cutoff) {
        log.info("Starting Extract, Unit: \"{}\",  User: {}", unit, cutoff, requestData.username());

        LocalDateTime startDate = getStartDate(cutoff);
        LocalDateTime endDate = getEndDate(cutoff);
        List<String> types = Arrays.stream(caseTypesMapping.get(unit)).map(Enum::toString).collect(Collectors.toList());

        Set<CaseDataAudit> caseDataAudits = caseDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<StageDataAudit> stageDataAudits = stageDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        Map<UUID, List<StageDataAudit>> groupedStages = stageDataAudits.stream().collect(Collectors.groupingBy(StageDataAudit::getCaseUUID));

        List<ExportLine> reportLines = caseDataAudits.stream().map(c -> getExportLine(c, groupedStages, header, objectMapper)).collect(Collectors.toList());

        log.info("Returned Extract, Found: {}, User: {}", caseDataAudits.size(), requestData.username());

        return reportLines;
    }

    private static String getReportingDataCSV(String header, List<ExportLine> exportLines) {
        StringBuilder sb = new StringBuilder();
        try(CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT)) {
            printer.printRecord(header);
            exportLines.forEach(l -> printExportLine(printer, l));
        } catch (IOException e) {
            log.warn(e.toString());
        }

        return sb.toString();
    }

    private static void printExportLine(CSVPrinter printer, ExportLine l) {
        try {
            printer.printRecord(l.getLineSchema().values());
        } catch (IOException e) {
            log.warn(e.toString());
        }
    }

    private static ExportLine getExportLine(CaseDataAudit caseDataAudit, Map<UUID, List<StageDataAudit>> groupedStages, String header, ObjectMapper objectMapper) {
        Map<String, String> ret = caseToMap(caseDataAudit);

        UUID caseUUID = caseDataAudit.getUuid();
        if (groupedStages.containsKey(caseUUID)) {
            ret.putAll(stagesToMap(groupedStages.get(caseUUID), objectMapper));
        }
        return ExportLine.from(header, ret);
    }

    private static Map<String, String> caseToMap(CaseDataAudit caseDataAudit) {
        Map<String, String> caseMap = new HashMap<>();

        String name = "Case";
        caseMap.put(columnNameFormat(name, "UUID"), caseDataAudit.getUuid().toString());
        caseMap.put(columnNameFormat(name, "Type"), caseDataAudit.getType());
        caseMap.put(columnNameFormat(name, "Timestamp"), caseDataAudit.getTimestamp().toString());
        caseMap.put(columnNameFormat(name, "Reference"), caseDataAudit.getReference());

        return caseMap;
    }

    private static Map<String, String> stagesToMap(List<StageDataAudit> auditStageDataList, ObjectMapper objectMapper) {
        Map<String, String> stageMap = new HashMap<>();

        auditStageDataList.forEach(auditStageData -> {
            String stageName = auditStageData.getType();
            stageMap.put(columnNameFormat(stageName, "UUID"), auditStageData.getUuid().toString());
            stageMap.put(columnNameFormat(stageName, "Type"), auditStageData.getType());
            stageMap.put(columnNameFormat(stageName, "Timestamp"), auditStageData.getTimestamp().toString());
            stageMap.put(columnNameFormat(stageName, "CaseUUID"), auditStageData.getCaseUUID().toString());
            try {
                Map<String, String> dataMap = objectMapper.readValue(auditStageData.getData(), new TypeReference<HashMap<String, String>>() {});
                dataMap.forEach((key, value) -> stageMap.put(columnNameFormat(stageName, key), value));
            } catch (IOException e) {
                log.error(e.toString());
            }
        });

        return stageMap;
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
