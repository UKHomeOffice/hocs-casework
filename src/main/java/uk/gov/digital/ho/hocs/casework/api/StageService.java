package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifiyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
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
    private final CaseNoteService caseNoteService;
    private final UserPermissionsService userPermissionsService;
    private final InfoClient infoClient;
    private final NotifyClient notifyClient;

    @Autowired
    public StageService(StageRepository stageRepository, CaseNoteService caseNoteService, UserPermissionsService userPermissionsService, InfoClient infoClient, NotifyClient notifyClient) {
        this.stageRepository = stageRepository;
        this.caseNoteService = caseNoteService;
        this.userPermissionsService = userPermissionsService;
        this.infoClient = infoClient;
        this.notifyClient = notifyClient;
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting User for Stage: {}", stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        log.debug("Got User: {} for Stage: {}", stage.getUserUUID(), stageUUID);
        return stage.getUserUUID();
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Team for Stage: {}", stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        log.debug("Got Team: {} for Stage: {}", stage.getTeamUUID(), stageUUID);
        return stage.getTeamUUID();
    }

    Stage getActiveStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Active Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Active Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, LocalDate deadline, String emailType) {
        log.debug("Creating Stage of type: {}", stageType);
        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);
        stageRepository.save(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, stage.getCaseReference(), emailType);
        return stage;
    }

    void updateStageDeadline(UUID caseUUID, UUID stageUUID, LocalDate deadline) {
        log.debug("Updating Deadline for Stage: {}", stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        stage.setDeadline(deadline);
        stageRepository.save(stage);
        log.info("Set Stage Deadline: {} ({}) for Case {}", stageUUID, deadline, caseUUID, value(EVENT, STAGE_DEADLINE_UPDATED));
    }

    void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID newTeamUUID, String emailType) {
        log.debug("Updating Team: {} for Stage: {}", newTeamUUID, stageUUID);
        // Check all stages because when rejecting back the stage will not be active.
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setTeam(newTeamUUID);
        stageRepository.save(stage);
        if (newTeamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID, value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, newTeamUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), newTeamUUID, stage.getCaseReference(), emailType);
        }
    }

    void updateStageUser(UUID caseUUID, UUID stageUUID, UUID newUserUUID) {
        log.debug("Updating User: {} for Stage: {}", newUserUUID, stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        UUID currentUserUUID = stage.getUserUUID();
        stage.setUser(newUserUUID);
        stageRepository.save(stage);
        log.info("Updated User: {} for Stage {}", newUserUUID, stageUUID, value(EVENT, STAGE_ASSIGNED_USER));
        notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, stage.getCaseReference());
    }

    Set<Stage> getActiveStagesByCaseUUID(UUID caseUUID) {
        log.debug("Getting Active Stages for Case: {}", caseUUID);
        return stageRepository.findAllActiveByCaseUUID(caseUUID);
    }
  
    Set<Stage> getActiveStagesByTeamUUID(UUID teamUUID) {
        log.debug("Getting Active Stages for Team: {}", teamUUID);
        return stageRepository.findAllActiveByTeamUUID(teamUUID);
    }

    Set<Stage> getActiveStagesForUser() {
        log.debug("Getting Active Stages for User");
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, STAGE_LIST_EMPTY));
            return new HashSet<>();
        } else {
            Set<Stage> stages = stageRepository.findAllActiveByTeamUUIDIn(teams);
            log.info("Returning {} Stages", stages.size(), value(EVENT, STAGE_LIST_RETRIEVED));
            return stages;
        }
    }

    private Stage getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            Set<String> caseNoteStageTypes = infoClient.getCaseNoteStageTypes(stage.getStageType());
            Set<CaseNote> caseNotes = caseNoteService.getLatestCaseNotes(stage.getCaseUUID(), caseNoteStageTypes);
            stage.setCaseNotes(caseNotes);
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID, value(EVENT, STAGE_RETRIEVED));
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

}