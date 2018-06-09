package uk.gov.digital.ho.hocs.casework.audit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.rsh.RshReportLine;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
class AuditService {

    private final AuditCaseDetailsRepository auditCaseDetailsRepository;
    private final AuditStageDetailsRepository auditStageDetailsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public AuditService(AuditCaseDetailsRepository auditCaseDetailsRepository, AuditStageDetailsRepository auditStageDetailsRepository) {
        this.auditCaseDetailsRepository = auditCaseDetailsRepository;
        this.auditStageDetailsRepository = auditStageDetailsRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    @Transactional
    public String extractData(String[] types, LocalDate cutoff, String username) {
        log.info("Requesting Extract, Types: \"{}\",  Cutoff: {}, User: {}", Arrays.toString(types), cutoff, username);

        int monthsBack = 4;
        // Start at the first day of the month
        LocalDateTime startDate = LocalDateTime.of(cutoff.minusMonths(monthsBack).getYear(),cutoff.minusMonths(monthsBack).getMonth(),1,0,0);
        LocalDateTime endDate = LocalDateTime.of(cutoff, LocalTime.MAX);

        Set<AuditCaseData> auditCaseData = auditCaseDetailsRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<AuditStageData> auditStageData = auditStageDetailsRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        List<Map<String,String>> reportLines = auditCaseData.stream().map( c ->  {
           Map<String,String> ret =  new HashMap<>();
           ret.putAll(caseToMap(c));
           ret.putAll(stagesToMap(auditStageData.stream().filter(s -> c.getUuid().equals(s.getCaseUUID())), objectMapper));
           return ret;
            }).map(l -> RshReportLine.from(l).getLineSchema()).collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        try {
            CSVPrinter printer = new CSVPrinter(sb, CSVFormat.DEFAULT);
            printer.printRecord(reportLines.get(0).keySet());
            for (Map<String, String> l : reportLines) {
                try {
                    printer.printRecord(l.values());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        log.info("Returned Extract, Found: {}, User: {}", auditCaseData.size(), username);
            return sb.toString();
        }

     private static Map<String,String> caseToMap(AuditCaseData auditCaseData){
        Map<String,String> caseMap = new HashMap<>();

        String name = "Case";
        caseMap.put(stageNameFormat(name, "Type"), auditCaseData.getType());
        caseMap.put(stageNameFormat(name, "Reference"), auditCaseData.getReference());
        caseMap.put(stageNameFormat(name, "UUID"), auditCaseData.getUuid().toString());
        caseMap.put(stageNameFormat(name, "Created"), auditCaseData.getCreated().toString());

        return caseMap;
    }

    private static Map<String,String> stagesToMap(Stream<AuditStageData> auditStageDataList, ObjectMapper objectMapper){
        Map<String,String> stageMap = new HashMap<>();

        auditStageDataList.forEach( auditStageData -> {
            String stageName = auditStageData.getName();
            stageMap.put(stageNameFormat(stageName, "UUID"), auditStageData.getUuid().toString());
            stageMap.put(stageNameFormat(stageName, "Name"), auditStageData.getName());
            stageMap.put(stageNameFormat(stageName, "CaseUUID"), auditStageData.getCaseUUID().toString());
            stageMap.put(stageNameFormat(stageName, "SchemaVersion"), auditStageData.getSchemaVersion() + "");
            stageMap.put(stageNameFormat(stageName, "Created"), auditStageData.getCreated().toString());
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

    private static String stageNameFormat(String prefix, String suffix){
        return String.format("%s_%s", prefix, suffix);
    }
}
