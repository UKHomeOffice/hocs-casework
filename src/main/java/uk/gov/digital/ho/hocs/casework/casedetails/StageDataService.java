package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StageDataService {

    private final StageDataRepository stageDataRepository;
    private final AuditService auditService;

    @Autowired
    public StageDataService(StageDataRepository stageDataRepository,
                            AuditService auditService) {

        this.stageDataRepository = stageDataRepository;
        this.auditService = auditService;
    }

    @Transactional
    public StageData createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        log.debug("Creating Stage, Type: {}, Case UUID: {}", stageType, caseUUID);
        StageData stageData = new StageData(caseUUID, stageType, teamUUID, userUUID);
        stageDataRepository.save(stageData);
        auditService.createStageEvent(caseUUID, stageType, teamUUID, userUUID);
        log.info("Created Stage UUID: {}, Type: {}, Case UUID: {}", stageData.getUuid(), stageData.getType(), stageData.getCaseUUID());
        return stageData;
    }


    @Transactional
    public Set<StageData> getStagesForCase(UUID caseUUID) {
        log.debug("Getting all Stages for Case: {}", caseUUID);
        Set<StageData> stageData = stageDataRepository.findAllByCaseUuid(caseUUID);
        log.debug("Got all Stages for Case: {}", caseUUID);
        return stageData;
    }

    @Transactional
    public void allocateStage(UUID stageUUID, UUID teamUUID, UUID userUUID) {
        log.debug("Allocating Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
        if (teamUUID == null) {
            throw new EntityCreationException("Cannot call allocateStage(%s, null, %s).", stageUUID, userUUID);
        } else if (userUUID == null) {
            stageDataRepository.allocateToTeam(stageUUID, teamUUID);
        } else {
            stageDataRepository.allocateToUser(stageUUID, teamUUID, userUUID);
        }
        auditService.allocateStageEvent(stageUUID, teamUUID, userUUID);
        log.info("Allocated Stage UUID: {}, User {}, Team {}", stageUUID, userUUID, teamUUID);
    }

    @Transactional
    public void closeStage(UUID stageUUID) {
        log.debug("Completing Stage UUID: {}", stageUUID);
        stageDataRepository.setInactive(stageUUID);
        auditService.setStageInactiveEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    @Transactional
    public void reopenStage(UUID stageUUID) {
        log.debug("Completing Stage UUID: {}", stageUUID);
        stageDataRepository.setActive(stageUUID);
        auditService.setStageActiveEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    @Transactional
    public StageData getStage(UUID stageUUID) {
        log.debug("Getting Stage UUID: {}", stageUUID);
        StageData stageData = getStageData(stageUUID);
        auditService.getStageEvent(stageUUID);
        log.info("Got Stage UUID: {}", stageData.getUuid());
        return stageData;
    }

    //TODO: This method is a dev tool
    public Set<StageData> getActiveStages() {
        return stageDataRepository.findAllActiveStages();
    }

    public Set<StageData> getActiveStagesByUserUUID(UUID userUUID) {
        return stageDataRepository.findAllActiveStages();
    }

    public Set<StageData> getActiveStagesByTeamUUID(Set<UUID> teamUUIDs) {
        return stageDataRepository.findAllActiveStages();
    }

    private StageData getStageData(UUID stageUUID) {
        log.debug("Getting Stage Data for Stage UUID: {}", stageUUID);
        StageData stageData = stageDataRepository.findByUuid(stageUUID);
        if (stageData != null) {
            log.debug("Got Stage Data for Case UUID: {}", stageData);
            return stageData;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID);
        }
    }

}