package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.HocsCaseApplication.isNullOrEmpty;

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

    private static String getDataString(Map<String, String> stageData, ObjectMapper objectMapper) {
        String data = null;
        try {
            data = objectMapper.writeValueAsString(stageData);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Transactional
    public CaseData createCase(String caseType, String username) throws EntityCreationException {
        log.info("Requesting Create Case, Type: {}, User: {}", caseType, username);
        if (!isNullOrEmpty(caseType)) {
            CaseData caseData = new CaseData(caseType, caseDataRepository.getNextSeriesId());
            auditService.writeCreateCaseEvent(username, caseData);
            caseDataRepository.save(caseData);
            log.info("Created Case, Reference: {}, UUID: {} User: {}", caseData.getReference(), caseData.getUuid(), username);
            return caseData;
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public StageData createStage(UUID caseUUID, String stageType, Map<String, String> stageData, String username) throws EntityCreationException {
        log.info("Requesting Create Stage, Type: {}, Case UUID: {}, User: {}", stageType, caseUUID, username);
        if (!isNullOrEmpty(caseUUID) && !isNullOrEmpty(stageType)) {
            String data = getDataString(stageData, objectMapper);
            StageData stageDetails = new StageData(caseUUID, stageType, data);
            auditService.writeCreateStageEvent(username, stageDetails);
            stageDataRepository.save(stageDetails);
            log.info("Created Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getType(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
            return stageDetails;
        } else {
            throw new EntityCreationException("Failed to create stage, invalid stageType or caseUUID!");
        }
    }

    @Transactional
    public CaseData updateCase(UUID caseUuid, String caseType, String username) throws EntityCreationException, EntityNotFoundException {
        log.info("Requesting Update Case: {}, Type: {}, User: {}", caseUuid, caseType, username);
        if (!isNullOrEmpty(caseUuid) && !isNullOrEmpty(caseType)) {
            CaseData caseData = caseDataRepository.findByUuid(caseUuid);
            if (caseData != null) {
                caseData.setType(caseType);
                auditService.writeUpdateCaseEvent(username, caseData);
                caseDataRepository.save(caseData);
                log.info("Updated Case, Reference: {}, UUID: {} User: {}", caseData.getReference(), caseData.getUuid(), username);
                return caseData;
            } else {
                throw new EntityNotFoundException("Case not found!");
            }
        } else {
            throw new EntityCreationException("Failed to create case, invalid caseType!");
        }
    }

    @Transactional
    public StageData updateStage(UUID caseUUID, UUID stageUUID, String stageType, Map<String, String> stageData, String username) throws EntityNotFoundException, EntityCreationException {
        log.info("Requesting Update Stage, uuid: {}, User: {}", stageUUID, username);
        if (!isNullOrEmpty(stageUUID) && !isNullOrEmpty(stageType)) {
        StageData stageDetails = stageDataRepository.findByUuid(stageUUID);
        if(stageDetails != null) {
            String data = getDataString(stageData, objectMapper);
            stageDetails.setType(stageType);
            stageDetails.setData(data);
            auditService.writeUpdateStageEvent(username, stageDetails);
            stageDataRepository.save(stageDetails);
            log.info("Updated Stage, UUID: {} ({}), Case UUID: {} User: {}", stageDetails.getType(), stageDetails.getUuid(), stageDetails.getCaseUUID(), username);
            return stageDetails;
        } else {
            throw new EntityNotFoundException("Stage not found!");
        }
        } else {
            throw new EntityCreationException("Failed to update stage, invalid caseType!");
        }
    }

    @Transactional
    public CaseData getCase(UUID uuid, String username) throws EntityNotFoundException {
        auditService.writeGetCaseEvent(username, uuid);
        log.info("Requesting Case, UUID: {}, User: {}", uuid, username);
        CaseData caseData = caseDataRepository.findByUuid(uuid);
        if (caseData == null) {
            throw new EntityNotFoundException("Case not Found!");
        }
        log.info("Found Case, Reference: {} ({}), User: {}", caseData.getReference(), caseData.getUuid(), username);
        return caseData;
    }
}