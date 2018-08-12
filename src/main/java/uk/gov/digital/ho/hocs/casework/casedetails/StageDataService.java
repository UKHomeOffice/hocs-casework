package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class StageDataService {

    private final AuditService auditService;
    private final ActiveStageService activeStageService;
    private final StageDataRepository stageDataRepository;
    private final CaseInputDataRepository caseInputDataRepository;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            ActiveStageService activeStageService,
                            CaseInputDataRepository caseInputDataRepository,
                            AuditService auditService) {

        this.stageDataRepository = stageDataRepository;
        this.caseInputDataRepository = caseInputDataRepository;
        this.activeStageService = activeStageService;
        this.auditService = auditService;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        log.debug("Creating Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        StageData stageData = new StageData(caseUUID, stageType);
        stageData.allocate(teamUUID, userUUID);
        stageDataRepository.save(stageData);
        activeStageService.allocateStage(stageData);
        auditService.writeCreateStageEvent(stageData);
        log.info("Created Stage UUID: {}, Type: {}, Case UUID: {}", stageData.getUuid(), stageData.getType(), stageData.getCaseUUID());
        return stageData;
    }

    @Transactional
    public void allocateStage(UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            stageData.allocate(teamUUID, userUUID);
            stageDataRepository.save(stageData);
            activeStageService.allocateStage(stageData);
            auditService.writeAllocateStageEvent(stageData);
            log.info("Allocated Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID.toString());
        }
    }

    public StageData getStage(UUID stageUUID) {
        log.debug("Getting Stage UUID: {}", stageUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        // TODO: Audit.
        if (stageData != null) {
            stageData.setCaseInputData(caseInputDataRepository.findByCaseUUID(stageData.getCaseUUID()));
            log.info("Got Stage UUID: {}", stageData.getUuid());
            return stageData;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID.toString());
        }
    }
}