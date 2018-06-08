package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyRequest;
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
    public CaseService( NotifyService notifyService, CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, AuditRepository auditRepository) {

        this.notifyService = notifyService;

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
        this.auditRepository = auditRepository;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    public CaseDetails createRshCase(Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
        CaseDetails caseDetails = createCase("RSH",  username);
        createStage(caseDetails.getUuid(),"OnlyStage", 0, caseData, username);

        if(caseDetails.getId() != 0) {
            notifyService.sendRshNotify(notifyRequest, caseDetails.getUuid());
        }
        return caseDetails;
    }

    public CaseDetails updateRshCase(UUID caseUUID, Map<String, Object> caseData, NotifyRequest notifyRequest, String username) {
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
    public CaseDetails createCase(String caseType, String username) {
        log.info("Requesting Create Case, Type: {}, User: {}", caseType, username);
        CaseDetails caseDetails = new CaseDetails(caseType, caseDetailsRepository.getNextSeriesId());
        AuditEntry auditEntry = new AuditEntry(username, caseDetails, null, AuditAction.CREATE_CASE);
        caseDetailsRepository.save(caseDetails);
        auditRepository.save(auditEntry);
        log.info("Created Case, Reference: {}, UUID: {} User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }

    @Transactional
    public StageDetails createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Create Stage, Name: {}, Case UUID: {}, User: {}", stageName, caseUUID, username);
        String data = getDataString(stageData, objectMapper);
        StageDetails stageDetails = new StageDetails(caseUUID, stageName, schemaVersion, data);
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.CREATE_STAGE);
        stageDetailsRepository.save(stageDetails);
        auditRepository.save(auditEntry);
        log.info("Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    public StageDetails updateStage(UUID stageUUID, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Update Stage, uuid: {}, User: {}", stageUUID, username);
        StageDetails stageDetails = stageDetailsRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData, objectMapper);
            stageDetails.setData(data);
            stageDetailsRepository.save(stageDetails);
        }
        AuditEntry auditEntry = new AuditEntry(username, null, stageDetails, AuditAction.UPDATE_STAGE);
        auditRepository.save(auditEntry);
        log.info("Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), auditEntry.getUsername());
        return stageDetails;
    }

    @Transactional
    public CaseDetails getRSHCase(UUID uuid, String username) {
        log.info("Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseDetails caseDetails = caseDetailsRepository.findByUuid(uuid);
        AuditEntry auditEntry = new AuditEntry(username, uuid.toString(), AuditAction.GET_CASE);
        auditRepository.save(auditEntry);
        log.info("Found Case, Reference: {} ({}), User: {}", caseDetails.getReference(), caseDetails.getUuid(), auditEntry.getUsername());
        return caseDetails;
    }

    private static String getDataString(Map<String, Object> stageData, ObjectMapper objectMapper) {
        String data = null;
        try {
            data = objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }
}