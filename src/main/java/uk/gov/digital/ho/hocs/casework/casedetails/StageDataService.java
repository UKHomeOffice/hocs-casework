package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;
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
    public Stage createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID);
        stageDataRepository.save(stage);
        auditService.createStageEvent(caseUUID, stageType, teamUUID, userUUID);
        log.info("Created Stage UUID: {}, Type: {}, Case UUID: {}", stage.getUuid(), stage.getType(), stage.getCaseUUID());
        return stage;
    }

    @Transactional
    public Set<Stage> getStagesForCase(UUID caseUUID) {
        Set<Stage> stageData = stageDataRepository.findAllByCaseUuid(caseUUID);
        log.info("Got all Stages for Case: {}", caseUUID);
        return stageData;
    }

    @Transactional
    public void allocateStage(UUID stageUUID, UUID teamUUID, UUID userUUID) {
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
        stageDataRepository.setInactive(stageUUID);
        auditService.setStageInactiveEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    @Transactional
    public void reopenStage(UUID stageUUID) {
        stageDataRepository.setActive(stageUUID);
        auditService.setStageActiveEvent(stageUUID);
        log.info("Completed Stage UUID: {}", stageUUID);
    }

    @Transactional
    public Stage getStage(UUID stageUUID) {
        Stage stage = getStageData(stageUUID);
        auditService.getStageEvent(stageUUID);
        log.info("Got Stage UUID: {}", stage.getUuid());
        return stage;
    }

    public Set<Stage> getActiveStages() {
        return stageDataRepository.findAllActiveStages();
    }

    public Set<Stage> getActiveStagesByUserUUID(UUID userUUID) {
        return stageDataRepository.findActiveStagesByUserUUID(userUUID);
    }

    public Set<Stage> getActiveStagesByTeamUUID(UUID teamUUID) {
        return stageDataRepository.findActiveStagesByTeamUUID(teamUUID);
    }

    private Stage getStageData(UUID stageUUID) {
        Stage stage = stageDataRepository.findByUuid(stageUUID);
        if (stage != null) {
            log.info("Got Stage Data for Case UUID: {}", stage);
            return stage;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID);
        }
    }

}