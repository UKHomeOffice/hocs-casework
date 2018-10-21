package uk.gov.digital.ho.hocs.casework.casedetails;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StageDataService {

    private final StageRepository stageRepository;

    @Autowired
    public StageDataService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public Stage createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getType(), stage.getCaseUUID());
        return stage;
    }

    @Transactional
    public void allocateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID) {
        if (teamUUID == null) {
            throw new EntityCreationException("Cannot call allocateStage(%s, %s, null, %s).", caseUUID, stageUUID, userUUID);
        } else if (userUUID == null) {
            stageRepository.allocateToTeam(caseUUID, stageUUID, teamUUID);
        } else {
            stageRepository.allocateToUser(caseUUID, stageUUID, teamUUID, userUUID);
        }
        log.info("Allocated Stage: {}, User {}, Team {} for Case {}", stageUUID, userUUID, teamUUID, caseUUID);
    }

    @Transactional
    public void completeStage(UUID caseUUID, UUID stageUUID) {
        stageRepository.complete(caseUUID, stageUUID);
        log.info("Completed Stage: {}", stageUUID);
    }

    @Transactional
    public Stage getStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = stageRepository.findByUuid(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID);
        }
    }

    public Set<Stage> getActiveStagesByUserUUID(UUID userUUID) {
        return stageRepository.findAllByUserUID(userUUID);
    }

    public Set<Stage> getActiveStagesByTeamUUID(UUID teamUUID) {
        return stageRepository.findAllByTeamUUID(teamUUID);
    }

}