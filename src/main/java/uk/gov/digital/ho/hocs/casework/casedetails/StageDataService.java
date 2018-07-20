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
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class StageDataService {

    private final AuditService auditService;
    private final ActiveStageDataService activeStageDataService;
    private final ActiveStageService activeStageService;
    private final StageDataRepository stageDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            ActiveStageDataService activeStageDataService,
                            ActiveStageService activeStageService,
                            AuditService auditService) {
        this.stageDataRepository = stageDataRepository;
        this.activeStageDataService = activeStageDataService;
        this.activeStageService = activeStageService;
        this.auditService = auditService;

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
            StageData stageData = new StageData(caseUUID, stageType.getStringValue(), dataString);
            // TODO: do we need to create the stage now or once the stage is complete?
            stageDataRepository.save(stageData);
            // TODO: GET Case
            activeStageService.addActiveStage(stageData.getUuid(), stageData.getType(), caseUUID, "PlumbMeIN", "MIN");
            auditService.writeCreateStageEvent(stageData);
            log.info("Created Stage, UUID: {} ({}), Case UUID: {}", stageData.getType(), stageData.getUuid(), stageData.getCaseUUID());
            return stageData;
        } else {
            throw new EntityCreationException("Failed to create stage, invalid stageType or caseUUID!");
        }
    }

    private static Map<String, String> stringToMap(String value, ObjectMapper objectMapper) {
        Map<String, String> dataMap = new HashMap<>();
        try {
            dataMap = objectMapper.readValue(value, new TypeReference<HashMap<String, String>>() {
            });
            return dataMap;
        } catch (IOException e) {
            log.error(e.toString());
        }
        return dataMap;
    }

    @Deprecated
    @Transactional
    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> data) {
        log.info("Requesting Update Stage, uuid: {}", stageUUID);
        if (!isNullOrEmpty(stageUUID) && data != null) {
            StageData stageData = stageDataRepository.findByUuid(stageUUID);
            if (stageData != null) {
                String dataString = getDataString(data, objectMapper);
                stageData.setData(dataString);
                stageDataRepository.save(stageData);
                auditService.writeUpdateStageEvent(stageData);
                log.info("Updated Stage, UUID: {} ({}), Case UUID: {}", stageData.getType(), stageData.getUuid(), stageData.getCaseUUID());
            } else {
                throw new EntityNotFoundException("Stage not found!");
            }
        } else {
            throw new EntityCreationException("Failed to update stage, invalid StageType!");
        }
    }

    @Transactional
    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.info("Requesting Update Stage, uuid: {}", stageUUID);
        if (!isNullOrEmpty(stageUUID)) {
            StageData stageData = stageDataRepository.findByUuid(stageUUID);
            if (stageData != null) {

                Set<ActiveStageData> activeStageData = activeStageDataService.getScreens(stageUUID);

                Map<String, String> data = activeStageData.stream().flatMap(s -> stringToMap(s.getData(), objectMapper).entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                // TODO: why update here when we can create?
                String dataString = getDataString(data, objectMapper);
                stageData.setData(dataString);
                stageDataRepository.save(stageData);
                activeStageDataService.removeScreens(stageUUID);
                activeStageService.removeActiveStage(stageUUID);
                auditService.writeUpdateStageEvent(stageData);

                log.info("Updated Stage, UUID: {} ({}), Case UUID: {}", stageData.getType(), stageData.getUuid(), stageData.getCaseUUID());
            } else {
                throw new EntityNotFoundException("Stage not found!");
            }
        } else {
            throw new EntityCreationException("Failed to update stage, invalid StageType!");
        }

    }
}