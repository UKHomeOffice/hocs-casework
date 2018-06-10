package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaseDataService {

    private final AuditService auditService;
    private final CaseDataRepository caseDataRepository;
    private final StageDataRepository stageDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseDataService(CaseDataRepository caseDataRepository, StageDataRepository stageDataRepository, AuditService auditService) {

        this.caseDataRepository = caseDataRepository;
        this.stageDataRepository = stageDataRepository;
        this.auditService = auditService;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    @Transactional
    public CaseData createCase(String caseType, String username) {
        log.info("CREATE CASE: Requesting Create Case, Type: {}, User: {}", caseType, username);
        CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
        auditService.writeCreateCaseEvent(username, caseData);
        caseDataRepository.save(caseData);
        log.info("CREATE CASE: Created Case, Reference: {}, UUID: {} User: {}", caseData.getReference(), caseData.getUuid(), username);
        return caseData;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String, Object> stageData, String username) {
        log.info("CREATE STAGE: Requesting Create Stage, Name: {}, Case UUID: {}, User: {}", stageName, caseUUID, username);
        String data = getDataString(stageData, objectMapper);
        StageData stageDetails = new StageData(caseUUID, stageName, schemaVersion, data);
        auditService.writeCreateStageEvent(username, stageDetails);
        stageDataRepository.save(stageDetails);
        log.info("CREATE STAGE: Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
        return stageDetails;
    }

    @Transactional
    public StageData updateStage(UUID stageUUID, int schemaVersion, Map<String, Object> stageData, String username) {
        log.info("UPDATE STAGE: Requesting Update Stage, uuid: {}, User: {}", stageUUID, username);
        StageData stageDetails = stageDataRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            stageDetails.setSchemaVersion(schemaVersion);
            String data = getDataString(stageData, objectMapper);
            stageDetails.setData(data);
            auditService.writeUpdateStageEvent(username, stageDetails);
            stageDataRepository.save(stageDetails);
            log.info("UPDATE STAGE: Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
        }
        return stageDetails;
    }

    @Transactional
    public CaseData getCase(UUID uuid, String username) {
        auditService.writeGetCaseEvent(username, uuid);
        log.info("GET CASE: Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseData caseData = caseDataRepository.findByUuid(uuid);
        log.info("GET CASE: Found Case, Reference: {} ({}), User: {}", caseData.getReference(), caseData.getUuid(), username);
        return caseData;
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