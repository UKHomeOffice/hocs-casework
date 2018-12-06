package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class StageService {

    private final StageRepository stageRepository;
    private final UserPermissionsService userPermissionsService;

    @Autowired
    public StageService(StageRepository stageRepository, UserPermissionsService userPermissionsService) {
        this.stageRepository = stageRepository;
        this.userPermissionsService = userPermissionsService;
    }

    public Stage getStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = stageRepository.findByUuid(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new EntityNotFoundException("Stage UUID: %s not found!", stageUUID);
        }
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        return stage.getUserUUID();
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        return stage.getTeamUUID();
    }

    public Stage createStage(UUID caseUUID, StageType stageType, UUID teamUUID, LocalDate deadline) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID());
        return stage;
    }

    public void updateDeadline(UUID caseUUID, UUID stageUUID, LocalDate deadline) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setDeadline(deadline);
        stageRepository.save(stage);
        log.info("Set Stage Deadline: {} ({}) for Case {}", stageUUID, deadline, caseUUID);
    }

    public void updateTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setTeam(teamUUID);
        stageRepository.save(stage);
        log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, teamUUID, caseUUID);
    }

    public void updateUser(UUID caseUUID, UUID stageUUID, UUID userUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setUser(userUUID);
        stageRepository.save(stage);
        log.info("Set Stage User: {} ({}) for Case {}", stageUUID, userUUID, caseUUID);
    }

    public void completeStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.completeStage();
        stageRepository.save(stage);
        log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID);
    }

    public Set<Stage> getActiveStagesByUserUUID(UUID userUUID) {
        return stageRepository.findAllByUserUUID(userUUID);
    }

    public Set<Stage> getActiveStagesByTeamUUID(UUID teamUUID) {
        return stageRepository.findAllByTeamUUID(teamUUID);
    }

    public Set<Stage> getActiveStages() {
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.info("Returning empty stage list");
            return new HashSet<>();
        } else {
            Set<Stage> stages = stageRepository.findAllBy(teams);
            log.info("Returning {} stages", stages.size());
            return stages;
        }
    }

}