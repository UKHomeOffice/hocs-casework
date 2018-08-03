package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class StageDataService {

    private final AuditService auditService;
    private final ActiveStageService activeStageService;
    private final CaseDataService caseDataService;
    private final StageDataRepository stageDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            ActiveStageService activeStageService,
                            CaseDataService caseDataService,
                            AuditService auditService) {
        this.auditService = auditService;
        this.activeStageService = activeStageService;
        this.caseDataService = caseDataService;
        this.stageDataRepository = stageDataRepository;

        //TODO: This should be a Bean
        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
    }

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) throws EntityCreationException {
        String data = "{}";
        if (stageData != null) {
            try {
                data = objectMapper.writeValueAsString(stageData);
            } catch (JsonProcessingException e) {
                throw new EntityCreationException("Object Mapper failed to parse!");
            }
        }

        return data;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, Map<String, String> data) {
        log.info("Requesting Create Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(stageType) && data != null) {
            String dataString = getDataString(data, objectMapper);
            // TODO: we should store the actual ENUM value.
            StageData stageData = new StageData(caseUUID, stageType.toString(), dataString);
            saveAndAudit(stageData);
            log.debug("Created Stage, UUID: {} ({}), Case UUID: {}", stageData.getType(), stageData.getUuid(), stageData.getCaseUUID());
            return stageData;
        } else {
            throw new EntityCreationException("Failed to create stage, invalid stageType or caseUUID!");
        }
    }

    @Transactional
    public void allocateStage(UUID caseUUID, UUID stageUUID) {
        log.info("Requesting Complete Stage, uuid: {}", stageUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        CaseData caseData = caseDataService.getCase(caseUUID);
        StageType stageType = StageType.valueOf(stageData.getType());
        CaseType caseType = CaseType.valueOf(caseData.getType());
        activeStageService.addActiveStage(caseUUID, stageData.getUuid(), caseData.getReference(), caseType, stageType, "", "", "", "");
        log.debug("Completed Stage, uuid: {}", stageUUID);
    }

    @Transactional
    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> newData) throws IOException {
        log.info("Requesting Update Stage, Case UUID: {} Stage UUID: {}", caseUUID, stageUUID);
        if (!isNullOrEmpty(stageUUID) && newData != null) {
            StageData stageData = stageDataRepository.findByUuid(stageUUID);
            if (stageData != null) {
                String dataString = updateDataString(newData, stageData.getData());
                stageData.setData(dataString);
                saveAndAudit(stageData);
                log.debug("Updated Stage, UUID: {} ({}), Case UUID: {}", stageData.getType(), stageData.getUuid(), stageData.getCaseUUID());
            } else {
                throw new EntityNotFoundException("Stage not found!");
            }
        } else {
            throw new EntityCreationException("Failed to update stage, invalid StageType!");
        }
    }

    @Transactional
    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.info("Requesting Complete Stage, uuid: {}", stageUUID);
        activeStageService.removeActiveStage(stageUUID);
        log.debug("Completed Stage, uuid: {}", stageUUID);
    }

    StageData getStage(UUID caseUUID, UUID stageUUID) {
        return stageDataRepository.findByUuid(stageUUID);
    }

    private void saveAndAudit(StageData stageData) {
        stageDataRepository.save(stageData);
        auditService.writeUpdateStageEvent(stageData);
    }

    private String updateDataString(Map<String, String> newData, String stageData) throws IOException {
        HashMap<String, String> data = objectMapper.readValue(stageData, new TypeReference<Map<String, String>>() {
        });
        data.putAll(newData);
        return getDataString(data, objectMapper);
    }
}