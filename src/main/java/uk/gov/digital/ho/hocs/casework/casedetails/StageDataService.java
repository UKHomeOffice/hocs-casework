package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.HocsCaseServiceConfiguration;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class StageDataService {

    private final AuditService auditService;
    private final ActiveStageService activeStageService;
    private final CaseDataRepository caseDataRepository;
    private final StageDataRepository stageDataRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            ActiveStageService activeStageService,
                            CaseDataRepository caseDataRepository,
                            AuditService auditService) {
        this.auditService = auditService;
        this.activeStageService = activeStageService;
        this.caseDataRepository = caseDataRepository;
        this.stageDataRepository = stageDataRepository;

        //TODO: This should be a Bean
        this.objectMapper = HocsCaseServiceConfiguration.initialiseObjectMapper(new ObjectMapper());
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, Map<String, String> data) {
        log.debug("Creating Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        StageData stageData = new StageData(caseUUID, caseData.getReference(), stageType, data, objectMapper);
        stageDataRepository.save(stageData);
        allocateStage(caseData, stageData);
        auditService.writeCreateStageEvent(stageData);
        log.info("Created Stage UUID: {} ({}), Case UUID: {}", stageData.getUuid(), stageData.getType(), stageData.getCaseUUID());
        return stageData;
    }

    public StageData getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            log.info("Got Stage UUID: {}, Case UUID: {}", stageData.getUuid(), stageData.getCaseUUID());
            return stageData;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s,  not found!", stageUUID.toString(), caseUUID.toString());
        }
    }

    @Transactional
    public void updateStage(UUID caseUUID, UUID stageUUID, Map<String, String> newData) {
        log.debug("Updating Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            stageData.updateData(newData, objectMapper);
            stageDataRepository.save(stageData);
            auditService.writeUpdateStageEvent(stageData);
            log.info("Updated Stage UUID: {}, Case UUID: {}", stageData.getUuid(), stageData.getCaseUUID());
        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s not found!", stageUUID.toString(), caseUUID.toString());
        }
    }

    public void allocateStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Allocating Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            CaseData caseData = caseDataRepository.findByUuid(caseUUID);
            allocateStage(caseData, stageData);
        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s not found!", stageUUID.toString(), caseUUID.toString());
        }
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Completing Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        activeStageService.removeActiveStage(caseUUID, stageUUID);
        log.info("Completed Stage, uuid: {}", stageUUID, caseUUID);
    }

    private void allocateStage(CaseData caseData, StageData stageData) {
        activeStageService.addActiveStage(caseData, stageData, "", "", "", "");
        log.info("Allocated Stage UUID: {}, Case UUID: {}, User {}, Team {}", stageData.getUuid(), caseData.getUuid(), "", "");
    }

}