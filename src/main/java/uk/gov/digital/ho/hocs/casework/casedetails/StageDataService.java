package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.util.UUID;

@Service
@Slf4j
public class StageDataService {

    private final StageDataRepository stageDataRepository;
    private final InputDataService inputDataService;
    private final AuditService auditService;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            InputDataService inputDataService,
                            AuditService auditService) {

        this.stageDataRepository = stageDataRepository;
        this.inputDataService = inputDataService;
        this.auditService = auditService;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        log.debug("Creating Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        StageData stageData = new StageData(caseUUID, stageType, teamUUID, userUUID);
        stageDataRepository.save(stageData);
        auditService.writeCreateStageEvent(caseUUID, stageType, teamUUID, userUUID);
        log.info("Created Stage UUID: {}, Type: {}, Case UUID: {}", stageData.getUuid(), stageData.getType(), stageData.getCaseUUID());
        return stageData;
    }

    @Transactional
    public void allocateStage(UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
        // TODO: this if should really be refactored into a 'User' model, revisit when we do the personService stuff.
        if (stageUUID == null) {
            throw new EntityNotFoundException("Cannot call allocateStage(null, %s, %s).", teamUUID, userUUID);
        }
        if (teamUUID == null) {
            throw new EntityCreationException("Cannot call allocateStage(%s, null, %s).", stageUUID, userUUID);
        }

        if (userUUID == null) {
            stageDataRepository.allocate(stageUUID, teamUUID);
        } else {
            stageDataRepository.allocate(stageUUID, teamUUID, userUUID);
        }

        auditService.writeAllocateStageEvent(stageUUID, teamUUID, userUUID);
        log.info("Allocated Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
    }

    @Transactional
    public void completeStage(UUID stageUUID) {
        log.debug("Completing Stage UUID: {}", stageUUID);
        stageDataRepository.setInactive(stageUUID);
        auditService.writeCompleteStageEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    @Transactional
    public StageData getStage(UUID stageUUID) {
        log.debug("Getting Stage UUID: {}", stageUUID);
        StageData stageData = getStageData(stageUUID);
        CaseInputData caseInputData = inputDataService.getInputData(stageData.getCaseUUID());
        stageData.setCaseInputData(caseInputData);
        auditService.writeGetStageEvent(stageUUID);
        log.info("Got Stage UUID: {}", stageData.getUuid());
        return stageData;
    }

    private StageData getStageData(UUID stageUUID) {
        log.debug("Getting Stage Data for Stage UUID: {}", stageUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            log.info("Got Stage Data for Case UUID: {}", stageData);
            return stageData;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID);
        }
    }

}