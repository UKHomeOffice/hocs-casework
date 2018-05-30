package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditAction;
import uk.gov.digital.ho.hocs.casework.audit.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.AuditRepository;
import uk.gov.digital.ho.hocs.casework.model.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.model.SearchRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import javax.transaction.Transactional;
import java.util.*;

@Service
@Slf4j
public class CaseService {



    private final NotifyService notifyService;

    private final AuditRepository auditRepository;
    private final CaseDetailsRepository caseDetailsRepository;
    private final StageDetailsRepository stageDetailsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseService( NotifyService notifyService, CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, AuditRepository auditRepository, ObjectMapper objectMapper) {

        this.notifyService = notifyService;

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
        this.auditRepository = auditRepository;

        this.objectMapper = objectMapper;
    }

    CaseDetails createRshCase(Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = createCase("RSH",  username);
        createStage(caseDetails.getUuid(),"OnlyStage", 0, caseData, username);

        if(caseDetails.getId() != 0) {
            try {
                notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
            }catch (Exception e) {
                // Do nothing.
            }
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
            try {
                notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
            }catch (Exception e) {
                // Do nothing.
            }
        }
        return caseDetails;
    }

    @Transactional
    CaseDetails createCase(String caseType, String username) {
        log.info("Requesting Create Case type: {}, User: {}", caseType, username);
        CaseDetails caseDetails = new CaseDetails(caseType, caseDetailsRepository.getNextSeriesId());
        AuditEntry auditEntry = new AuditEntry(username, caseDetails, null, AuditAction.CREATE_CASE);
        caseDetailsRepository.save(caseDetails);
        auditRepository.save(auditEntry);
        log.info("Created Case reference: {}, UUID: {} User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }

    @Transactional
    StageDetails createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Create Stage Name: {}, Case UUID: {}, User: {}", stageName, caseUUID, username);
        String data = getDataString(stageData);
        StageDetails stageDetails = new StageDetails(caseUUID, stageName, schemaVersion, data);
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.CREATE_STAGE);
        stageDetailsRepository.save(stageDetails);
        auditRepository.save(auditEntry);
        log.info("Created Stage UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    StageDetails updateStage(UUID stageUUID, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Update Stage uuid: {}, User: {}", stageUUID, username);
        StageDetails stageDetails = stageDetailsRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData);
            stageDetails.setData(data);
            stageDetailsRepository.save(stageDetails);
        }
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.UPDATE_STAGE);
        auditRepository.save(auditEntry);
        log.info("Updated Stage UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    CaseDetails getRSHCase(UUID uuid, String username) {
        log.info("Requesting Case uuid: {}, User: {}", uuid, username);
        CaseDetails caseDetails = caseDetailsRepository.findByUuid(uuid);
        AuditEntry auditEntry = new AuditEntry(username, uuid.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
        log.info("Found Case reference: {} ({}), User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }


    @Transactional
    List<CaseDetails> findCases(SearchRequest searchRequest, String username){
        String request = searchRequest.toJsonString(objectMapper);
        log.info("Requesting Search : {}, User: {}", request, username);
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
        return results;
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

