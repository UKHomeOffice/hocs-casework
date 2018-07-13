package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ScreenData;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.ScreenDataRepository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

@Service
@Slf4j
public class ScreenDataService {

    private final ScreenDataRepository screenDataRepository;

    private final ObjectMapper objectMapper;

    @Autowired
    public ScreenDataService(ScreenDataRepository screenDataRepository) {
        this.screenDataRepository = screenDataRepository;

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
    public void createScreenForStage(UUID stageUUID, String screenName, Map<String, String> data) {

        log.info("Creating Screen, Stage: {}, Name: {}", stageUUID, screenName);
        if (!isNullOrEmpty(stageUUID) && !isNullOrEmpty(screenName) && data != null) {
            String dataString = getDataString(data, objectMapper);
            ScreenData screenData = new ScreenData(screenName, dataString, stageUUID);
            screenDataRepository.save(screenData);
            log.info("Created Screen, Stage: {}, Name: {}", stageUUID, screenName);
        } else {
            throw new EntityCreationException("Failed to create Screen, invalid stageUUID or screenName!");
        }
    }

    @Transactional
    public Set<ScreenData> getScreens(UUID stageUUID) {

        log.info("Getting all Screens for Stage: {}", stageUUID);
        if (!isNullOrEmpty(stageUUID)) {
            Set<ScreenData> screenData = screenDataRepository.findAllByStageUUID(stageUUID);
            log.info("All Screens Got for Stage: {}", stageUUID);
            return screenData;
        } else {
            throw new EntityCreationException("Failed to remove Screens, invalid stageUUID!");
        }
    }

    @Transactional
    public void removeScreens(UUID stageUUID) {

        log.info("Removing all Screens for Stage: {}", stageUUID);
        if (!isNullOrEmpty(stageUUID)) {
            screenDataRepository.deleteAllByStageUUID(stageUUID);
            log.info("All Screens Removed for Stage: {}", stageUUID);
        } else {
            throw new EntityCreationException("Failed to remove Screens, invalid stageUUID!");
        }
    }

}