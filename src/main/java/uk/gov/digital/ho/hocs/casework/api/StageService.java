package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.notifiyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
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

    @Transactional
    public Stage getStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = stageRepository.findByUuid(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    @Transactional
    public Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, LocalDate deadline, String allocationType) {
        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);
        stageRepository.save(stage);
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, stage.getCaseReference(), allocationType);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        return stage;
    }

    @Transactional
    public void updateDeadline(UUID caseUUID, UUID stageUUID, LocalDate deadline) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setDeadline(deadline);
        stageRepository.save(stage);
        log.info("Set Stage Deadline: {} ({}) for Case {}", stageUUID, deadline, caseUUID, value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    @Transactional
    public void updateTeam(UUID caseUUID, UUID stageUUID, UUID teamUUID, String allocationType) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setTeam(teamUUID);
        stageRepository.save(stage);
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, stage.getCaseReference(), allocationType);
        log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, teamUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_TEAM));
    }

    @Transactional
    public void updateUser(UUID caseUUID, UUID stageUUID, UUID newUserUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        UUID currentUserUUID = stage.getUserUUID();
        if ((newUserUUID != null && !newUserUUID.equals(currentUserUUID)) || (currentUserUUID != null && !currentUserUUID.equals(newUserUUID))) {
            stage.setUser(newUserUUID);
            stageRepository.save(stage);
            log.info("Set Stage User: {} ({}) for Case {}", stageUUID, newUserUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_USER));
            notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, stage.getCaseReference());
        }
    }

    @Transactional
    public void completeStage(UUID caseUUID, UUID stageUUID) {
        Stage stage = getStage(caseUUID, stageUUID);
        stage.completeStage();
        stageRepository.save(stage);
        log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID, value(EVENT, STAGE_COMPLETED));
    }

    public Set<Stage> getActiveStagesByCaseUUID(UUID caseUUID) { return stageRepository.findActiveStagesByCaseUUID(caseUUID); }

    public Set<Stage> getActiveStages() {
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.info("Returning empty stage list", value(EVENT, STAGE_LIST_EMPTY));
            return new HashSet<>();
        } else {
            Set<Stage> stages = stageRepository.findAllBy(teams);
            log.info("Returning {} stages", stages.size(), value(EVENT, STAGE_LIST_RETRIEVED));
            return stages;
        }
    }

}