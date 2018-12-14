package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.notifiyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Slf4j
@Service
public class StageService {

    private final StageRepository stageRepository;
    private final UserPermissionsService userPermissionsService;
    private final NotifyClient notifyClient;

    @Autowired
    public StageService(StageRepository stageRepository, UserPermissionsService userPermissionsService, NotifyClient notifyClient) {
        this.stageRepository = stageRepository;
        this.userPermissionsService = userPermissionsService;
        this.notifyClient = notifyClient;
    }

    public Stage getStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    public Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, LocalDate deadline, String allocationType) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);
        stageRepository.save(stage);
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, stage.getCaseReference(), allocationType);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        return stage;
    }

    public void updateStageDeadline(UUID caseUUID, UUID stageUUID, LocalDate deadline) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setDeadline(deadline);
        stageRepository.save(stage);
        log.info("Set Stage Deadline: {} ({}) for Case {}", stageUUID, deadline, caseUUID, value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        return stageRepository.findStageUserUUID(caseUUID, stageUUID);
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        return stageRepository.findStageTeamUUID(caseUUID, stageUUID);
    }

    public void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        // This can happen on a reject path, the problem is getStages uses a view that filters out completed stages.
        // so we have to not use the Active_Stages view.
        Stage stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        stage.setTeam(teamUUID);
        stageRepository.save(stage);
        if (teamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID, value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, teamUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, stage.getCaseReference(), allocationType);
        }
    }

    public void updateStageUser(UUID caseUUID, UUID stageUUID, UUID newUserUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        UUID currentUserUUID = stage.getUserUUID();
        stage.setUser(newUserUUID);
        stageRepository.save(stage);
        log.info("Set Stage User: {} ({}) for Case {}", stageUUID, newUserUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_USER));
        notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, stage.getCaseReference());
    }

    public Set<Stage> getActiveStagesByCaseUUID(UUID caseUUID) {
        return stageRepository.findAllActiveByCaseUUID(caseUUID);
    }

    public Set<Stage> getActiveStagesForUser() {
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.info("Returning empty stage list", value(EVENT, STAGE_LIST_EMPTY));
            return new HashSet<>();
        } else {
            Set<Stage> stages = stageRepository.findAllActiveByTeamUUIDIn(teams);
            log.info("Returning {} stages", stages.size(), value(EVENT, STAGE_LIST_RETRIEVED));
            return stages;
        }
    }

}