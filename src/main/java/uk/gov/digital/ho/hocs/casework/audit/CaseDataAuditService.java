package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.model.CaseDataAudit;
import uk.gov.digital.ho.hocs.casework.audit.model.ExportLine;
import uk.gov.digital.ho.hocs.casework.audit.model.StageDataAudit;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CaseDataAuditService {

    private static Map<String, String[]> caseTypesMapping;
    private static Map<String, String> caseSchemaMapping;

    static {
        caseTypesMapping = new HashMap<>();
        caseTypesMapping.put("RSH", new String[]{"RSH"});
        caseTypesMapping.put("DCU", new String[]{"MIN", "TRO", "DTEN"});
        caseTypesMapping.put("UKVI", new String[]{"IMCB", "IMCM", "UTEN"});
        caseTypesMapping.put("FOI", new String[]{"FOI", "FTC", "FTCI", "FSC", "FSCI"});
        caseTypesMapping.put("HMPOCOR", new String[]{"COM", "COM1", "COM2", "DGEN", "GNR"});
        caseTypesMapping.put("HMPOCOL", new String[]{"COL"});

        caseSchemaMapping = new HashMap<>();
        caseSchemaMapping.put("RSH", "Case_Type,Case_Reference,Case_UUID,Case_Timestamp,Stage_legacy-reference,Stage_Name,Stage_UUID,Stage_SchemaVersion,Stage_Timestamp,Stage_who-calling,Stage_rep-first-name,Stage_rep-last-name,Stage_rep-org,Stage_rep-relationship,Stage_rep-calledfrom,Stage_contact-method-helpline,Stage_contact-method-method-mp,Stage_contact-method-media,Stage_contact-method-ie,Stage_contact-method-email,Stage_contact-method-als,Stage_contact-method-internal,Stage_contact-method-external,Stage_call-regarding-citizenship,Stage_call-regarding-settled,Stage_call-regarding-compensation,Stage_call-regarding-other,Stage_first-name,Stage_middle-name,Stage_last-name,Stage_date-of-birth,Stage_nationality-birth,Stage_nationality-current,Stage_address-1,Stage_address-2,Stage_address-town,Stage_post-code,Stage_dependents,Stage_dependents-how-many,Stage_high-profile,Stage_safeguarding,Stage_share-data,Stage_landing-date-day,Stage_landing-date-month,Stage_landing-date-year,Stage_cohort,Stage_date-left,Stage_country-based,Stage_date-last-travelled,Stage_nino,Stage_employment,Stage_education,Stage_tax,Stage_health,Stage_id-docs,Stage_travel-to-psc,Stage_psc-location,Stage_psc-date,Stage_psc-outcome,Stage_psc-followup,Stage_mp,Stage_media,Stage_outcome,Stage_notify-email");
    }

    private final AuditService auditService;
    private final CaseDataAuditRepository caseDataAuditRepository;
    private final StageDataAuditRepository stageDataAuditRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDataAuditService(AuditService auditService, CaseDataAuditRepository caseDataAuditRepository, StageDataAuditRepository stageDataAuditRepository) {
        this.auditService = auditService;
        this.caseDataAuditRepository = caseDataAuditRepository;
        this.stageDataAuditRepository = stageDataAuditRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    private static Map<String, String> caseToMap(CaseDataAudit caseDataAudit) {
        Map<String, String> caseMap = new HashMap<>();

        String name = "Case";
        caseMap.put(stageNameFormat(name, "Type"), caseDataAudit.getType());
        caseMap.put(stageNameFormat(name, "Reference"), caseDataAudit.getReference());
        caseMap.put(stageNameFormat(name, "UUID"), caseDataAudit.getUuid().toString());
        caseMap.put(stageNameFormat(name, "Timestamp"), caseDataAudit.getTimestamp().toString());

        return caseMap;
    }

    private static Map<String, String> stagesToMap(List<StageDataAudit> auditStageDataList, ObjectMapper objectMapper) {
        Map<String, String> stageMap = new HashMap<>();

        auditStageDataList.forEach(auditStageData -> {
            String stageName = auditStageData.getType();
            stageMap.put(stageNameFormat(stageName, "UUID"), auditStageData.getUuid().toString());
            stageMap.put(stageNameFormat(stageName, "Type"), auditStageData.getType());
            stageMap.put(stageNameFormat(stageName, "CaseUUID"), auditStageData.getCaseUUID().toString());
            stageMap.put(stageNameFormat(stageName, "Timestamp"), auditStageData.getTimestamp().toString());
            try {
                Map<String, String> dataMap = objectMapper.readValue(auditStageData.getData(), new TypeReference<HashMap<String, String>>() {
                });
                dataMap.forEach((key, value) -> stageMap.put(stageNameFormat(stageName, key), value));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        return stageMap;
    }

    private static LocalDateTime getEndDate(LocalDate cutoff) {
        return LocalDateTime.of(cutoff, LocalTime.MAX);
    }

    private static LocalDateTime getStartDate(LocalDate cutoff) {
        int monthsBack = 4;
        // Start at the first day of the month
        return LocalDateTime.of(cutoff.minusMonths(monthsBack).getYear(), cutoff.minusMonths(monthsBack).getMonth(), 1, 0, 0);
    }

    private static String stageNameFormat(String prefix, String suffix) {
        return String.format("%s_%s", prefix, suffix);
    }

    String getReportingDataAsCSV(String unit, LocalDate cutoff, String username) {
        if (caseTypesMapping.containsKey(unit)) {
            List<ExportLine> exportLines = getReportingData(unit, cutoff, username);

            StringBuilder sb = new StringBuilder();
            try {
                CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
                printer.printRecord(caseSchemaMapping.get(unit));
                exportLines.forEach(l -> {
                    try {
                        printer.printRecord(l.getLineSchema().values());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            return sb.toString();
        } else {
            return "";
        }
    }

    List<ExportLine> getReportingDataAsJson(String unit, LocalDate cutoff, String username) {
        if (caseTypesMapping.containsKey(unit)) {
            return getReportingData(unit, cutoff, username);
        } else {
            return new ArrayList<>();
        }
    }

    private List<ExportLine> getReportingData(String unit, LocalDate cutoff, String username) {
        String[] types = caseTypesMapping.get(unit);
        String typesAuditString = Arrays.toString(types).concat(cutoff.toString());
        auditService.writeExtractEvent(username, typesAuditString);
        log.info("Requesting Extract, Values: \"{}\",  User: {}", typesAuditString, cutoff, username);

        LocalDateTime startDate = getStartDate(cutoff);
        LocalDateTime endDate = getEndDate(cutoff);

        Set<CaseDataAudit> caseDatumAudits = caseDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<StageDataAudit> stageDatumAudits = stageDataAuditRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        Map<UUID, List<StageDataAudit>> groupedStages = stageDatumAudits.stream().collect(Collectors.groupingBy(StageDataAudit::getCaseUUID));

        Stream<ExportLine> reportLines = caseDatumAudits.stream().map(c -> {
            Map<String, String> ret = new HashMap<>(caseToMap(c));
            if (groupedStages.containsKey(c.getUuid())) {
                ret.putAll(stagesToMap(groupedStages.get(c.getUuid()), objectMapper));
            }
            return ret;
        }).map(l -> ExportLine.from(caseSchemaMapping.get(unit), l));

        log.info("Returned Extract, Found: {}, User: {}", caseDatumAudits.size(), username);
        return reportLines.collect(Collectors.toList());
    }
}
