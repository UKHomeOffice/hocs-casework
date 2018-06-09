package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class CaseService {

    private final AuditService auditService;
    private final CaseDetailsRepository caseDetailsRepository;
    private final StageDetailsRepository stageDetailsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public CaseService(CaseDetailsRepository caseDetailsRepository, StageDetailsRepository stageDetailsRepository, AuditService auditService) {

        this.caseDetailsRepository = caseDetailsRepository;
        this.stageDetailsRepository = stageDetailsRepository;
        this.auditService = auditService;

        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());

    }

    @Transactional
    public CaseDetails createCase(String caseType, String username) {
        log.info("Requesting Create Case, Type: {}, User: {}", caseType, username);
        CaseDetails caseDetails = new CaseDetails(caseType, caseDetailsRepository.getNextSeriesId());
        auditService.writeCreateCaseEvent(username, caseDetails);
        caseDetailsRepository.save(caseDetails);
        log.info("Created Case, Reference: {}, UUID: {} User: {}", caseDetails.getReference(), caseDetails.getUuid(), username);
        return caseDetails;
    }

    @Transactional
    public StageDetails createStage(UUID caseUUID, String stageName, int schemaVersion, Map<String,Object> stageData, String username) {
        log.info("Requesting Create Stage, Name: {}, Case UUID: {}, User: {}", stageName, caseUUID, username);
        String data = getDataString(stageData, objectMapper);
        StageDetails stageDetails = new StageDetails(caseUUID, stageName, schemaVersion, data);
        auditService.writeCreateStageEvent(username, stageDetails);
        stageDetailsRepository.save(stageDetails);
        log.info("Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
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
            auditService.writeUpdateStageEvent(username, stageDetails);
            stageDetailsRepository.save(stageDetails);
            log.info("Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getName(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
        }
        return stageDetails;
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