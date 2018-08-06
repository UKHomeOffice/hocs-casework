package uk.gov.digital.ho.hocs.casework.casedetails;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
                            AuditService auditService,
                            ObjectMapper objectMapper) {
        this.auditService = auditService;
        this.activeStageService = activeStageService;
        this.caseDataRepository = caseDataRepository;
        this.stageDataRepository = stageDataRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID, Map<String, String> data) {
        log.debug("Creating Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        CaseData caseData = caseDataRepository.findByUuid(caseUUID);
        if (caseData != null) {
            StageData stageData = new StageData(caseData, stageType, data, objectMapper);
            stageData.allocate(teamUUID, userUUID);
            stageDataRepository.save(stageData);
            activeStageService.addActiveStage(caseData, stageData, teamUUID, userUUID);
            auditService.writeCreateStageEvent(stageData);
            log.info("Created Stage UUID: {} ({}), Case UUID: {}", stageData.getUuid(), stageData.getType(), stageData.getCaseUUID());
            return stageData;
        } else {
            throw new EntityNotFoundException("Case UUID: %s, not found!", caseUUID.toString());
        }
    }

    public StageData getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            log.info("Got Stage UUID: {}, Case UUID: {}", stageData.getUuid(), stageData.getCaseUUID());
            return stageData;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s not found!", stageUUID.toString(), caseUUID.toString());
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

    @Transactional
    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            CaseData caseData = caseDataRepository.findByUuid(stageData.getCaseUUID());
            stageData.allocate(teamUUID, userUUID);
            stageDataRepository.save(stageData);
            // TODO: Audit.
            activeStageService.addActiveStage(caseData, stageData, teamUUID, userUUID);
            log.info("Allocated Stage UUID: {}, Case UUID: {}, User {}, Team {}", stageData.getUuid(), caseData.getUuid(), "", "");

        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s not found!", stageUUID.toString(), caseUUID.toString());
        }
    }

    @Transactional
    public void completeStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Completing Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            stageData.unallocate();
            stageDataRepository.save(stageData);
            // TODO: Audit.
            activeStageService.removeActiveStage(caseUUID, stageUUID);
            log.info("Completed Stage UUID: {}, Case UUID: {}", stageUUID, caseUUID);
        } else {
            throw new EntityNotFoundException("Stage UUID: %s, Case UUID: %s not found!", stageUUID.toString(), caseUUID.toString());
        }
    }
}