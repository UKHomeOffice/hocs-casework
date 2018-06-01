package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.AuditRepository;
import uk.gov.digital.ho.hocs.casework.model.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import javax.transaction.Transactional;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CaseService {



    private final NotifyService notifyService;

    private final AuditRepository auditRepository;
    private final CaseDetailsRepository caseDetailsRepository;
    private final StageDetailsRepository stageDetailsRepository;
    private final AuditCaseDetailsRepository auditCaseDetailsRepository;
    private final AuditStageDetailsRepository auditStageDetailsRepository;
    private final ObjectMapper objectMapper;
    private final CsvMapper csvMapper;

    @Autowired
    public CaseService( NotifyService notifyService, CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, AuditRepository auditRepository, AuditCaseDetailsRepository auditCaseDetailsRepository, AuditStageDetailsRepository auditStageDetailsRepository) {

        this.notifyService = notifyService;

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
        this.auditRepository = auditRepository;
        this.auditCaseDetailsRepository = auditCaseDetailsRepository;
        this.auditStageDetailsRepository = auditStageDetailsRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
        this.csvMapper = new CsvMapper();

    }

    CaseDetails createRshCase(Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = createCase("RSH",  username);
        createStage(caseDetails.getUuid(),"Stage", 0, caseData, username);

        if(caseDetails.getId() != 0) {
            notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
        }
        return caseDetails;
    }

    CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = getRSHCase(caseUUID, username);
        if(!caseDetails.getStages().isEmpty()) {
            StageDetails stageDetails = caseDetails.getStages().iterator().next();
            updateStage(stageDetails.getUuid(),0,caseData, username);
        }

        if(caseDetails.getId() != 0) {
            notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
        }
        return caseDetails;
    }

    @Transactional
    CaseDetails createCase(String caseType, String username) {
        log.info("Requesting Create Case, Type: {}, User: {}", caseType, username);
        CaseDetails caseDetails = new CaseDetails(caseType, caseDetailsRepository.getNextSeriesId());
        AuditEntry auditEntry = new AuditEntry(username, caseDetails, null, AuditAction.CREATE_CASE);
        caseDetailsRepository.save(caseDetails);
        auditRepository.save(auditEntry);
        log.info("Created Case, Reference: {}, UUID: {} User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }

    @Transactional
    StageDetails createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Create Stage, Name: {}, Case UUID: {}, User: {}", stageName, caseUUID, username);
        String data = getDataString(stageData);
        StageDetails stageDetails = new StageDetails(caseUUID, stageName, schemaVersion, data);
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.CREATE_STAGE);
        stageDetailsRepository.save(stageDetails);
        auditRepository.save(auditEntry);
        log.info("Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    StageDetails updateStage(UUID stageUUID, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Update Stage, uuid: {}, User: {}", stageUUID, username);
        StageDetails stageDetails = stageDetailsRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData);
            stageDetails.setData(data);
            stageDetailsRepository.save(stageDetails);
        }
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.UPDATE_STAGE);
        auditRepository.save(auditEntry);
        log.info("Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    CaseDetails getRSHCase(UUID uuid, String username) {
        log.info("Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseDetails caseDetails = caseDetailsRepository.findByUuid(uuid);
        AuditEntry auditEntry = new AuditEntry(username, uuid.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
        log.info("Found Case, Reference: {} ({}), User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }


    @Transactional
    List<CaseDetails> findCases(SearchRequest searchRequest, String username){
        String request = searchRequest.toJsonString(objectMapper);
        log.info("Requesting Search, User: {}", username);
        ArrayList<CaseDetails> results = new ArrayList<>();
        if(searchRequest.getCaseReference() != null){
            Set<CaseDetails> result = caseDetailsRepository.findByCaseReference(searchRequest.getCaseReference());
            if(result != null){
                results.addAll(result);
            }
        }
        if (results.size() == 0 && searchRequest.getCaseData() != null){
            Map<String, Object> searchData = searchRequest.getCaseData();
            Set<CaseDetails> resultList = caseDetailsRepository.findByNameOrDob(getFieldString(searchData,"first-name"), getFieldString(searchData,"last-name"), getFieldString(searchData,"date-of-birth"));
            results.addAll(resultList);
        }

        AuditEntry auditEntry = new AuditEntry(username, request, AuditAction.SEARCH);
        auditRepository.save(auditEntry);
        log.info("Returned Search, Found: {}, User: {}", results.size(), username);
        return results;
    }



    @Transactional
    String extractData(String[] types, LocalDate cutoff, String username) {
        log.info("Requesting Extract, Types: \"{}\",  Cutoff: {}, User: {}", Arrays.toString(types), cutoff, username);

        int monthsBack = 4;
        // Start at the first day of the month
        LocalDateTime startDate = LocalDateTime.of(cutoff.minusMonths(monthsBack).getYear(),cutoff.minusMonths(monthsBack).getMonth(),1,0,0);
        LocalDateTime endDate = LocalDateTime.of(cutoff, LocalTime.MAX);

        Set<AuditCaseData> auditCaseData = auditCaseDetailsRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);
        Set<AuditStageData> auditStageData = auditStageDetailsRepository.getAllByTimestampBetweenAndCorrespondenceTypeIn(startDate, endDate, types);

        List<RshReportLine> reportLines = auditCaseData.stream().map( c ->  {
           Map<String,String> ret =  new HashMap<>();
           ret.putAll(caseToMap(c));
           ret.putAll(stagesToMap(auditStageData.stream().filter(s -> c.getUuid().equals(s.getCaseUUID()))));
           return ret;
            }).map(RshReportLine::from).collect(Collectors.toList());

        CsvSchema schema = csvMapper.schemaFor(RshReportLine.class).withHeader();
        String value;
        try {
            value = csvMapper.writer(schema).writeValueAsString(reportLines);
        } catch (JsonProcessingException e) {
            return "";
        }

        log.info("Returned Extract, Found: {}, User: {}", auditCaseData.size(), username);
        return value;
    }

    private Map<String,String> caseToMap(AuditCaseData auditCaseData){
        Map<String,String> caseMap = new LinkedHashMap<>();

        String name = "Case";
        caseMap.put(stageNameFormat(name, "Type"), auditCaseData.getType());
        caseMap.put(stageNameFormat(name, "Reference"), auditCaseData.getReference());
        caseMap.put(stageNameFormat(name, "UUID"), auditCaseData.getUuid().toString());
        caseMap.put(stageNameFormat(name, "Created"), auditCaseData.getCreated().toString());

        return caseMap;
    }

    private Map<String,String> stagesToMap(Stream<AuditStageData> auditStageDataList){
        Map<String,String> stageMap = new LinkedHashMap<>();

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

    private String getFieldString(Map<String, Object> stageData, String key) {
        String ret = "";
        if(stageData.containsKey(key)){
            String val = stageData.get(key).toString();
            if(val != null) {
                ret = val;
            }
        }
        return ret;
    }

    private String getDataString(Map<String, Object> stageData) {
        String data = null;
        try {
            data = objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }
}

