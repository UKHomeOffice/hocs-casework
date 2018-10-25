package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import javax.transaction.Transactional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StageService {

    private final StageRepository stageRepository;

    @Autowired
    public StageService(StageRepository stageRepository) {
        this.stageRepository = stageRepository;
    }

    @Transactional
    public Stage createStage(UUID caseUUID, StageType stageType, UUID teamUUID, UUID userUUID, StageStatusType stageStatusType) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, stageStatusType);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID());
        return stage;
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

    @Transactional
    public void updateStage(UUID caseUUID, UUID stageUUID, UUID teamUUID, UUID userUUID, StageStatusType stageStatusType) {
        stageRepository.update(caseUUID, stageUUID, teamUUID, userUUID, stageStatusType);
        log.info("Updated Stage: {} ({}), User {}, Team {} for Case {}", stageUUID, stageStatusType, userUUID, teamUUID, caseUUID);
    }

    public Set<Stage> getActiveStagesByUserUUID(UUID userUUID) {
        return stageRepository.findAllByUserUID(userUUID);
    }

    public Set<Stage> getActiveStagesByTeamUUID(UUID teamUUID) {
        return stageRepository.findAllByTeamUUID(teamUUID);
    }

}