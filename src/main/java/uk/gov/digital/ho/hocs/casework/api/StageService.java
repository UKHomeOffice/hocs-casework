package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.json.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchClient.SearchClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;

@Slf4j
@Service
public class StageService {

    private final StageRepository stageRepository;
    private final UserPermissionsService userPermissionsService;
    private final NotifyClient notifyClient;
    private final AuditClient auditClient;
    private final SearchClient searchClient;
    private final InfoClient infoClient;
    private final CaseDataService caseDataService;

    private static final Comparator<Stage> CREATED_COMPARATOR = Comparator.comparing(Stage :: getCreated);


    @Autowired
    public StageService(StageRepository stageRepository, UserPermissionsService userPermissionsService, NotifyClient notifyClient, AuditClient auditClient, SearchClient searchClient, InfoClient infoClient, CaseDataService caseDataService) {
        this.stageRepository = stageRepository;
        this.userPermissionsService = userPermissionsService;
        this.notifyClient = notifyClient;
        this.auditClient = auditClient;
        this.searchClient = searchClient;
        this.infoClient = infoClient;
        this.caseDataService = caseDataService;
    }

    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting User for Stage: {}", stageUUID);
        Stage stage = getStage(caseUUID, stageUUID);
        log.debug("Got User: {} for Stage: {}", stage.getUserUUID(), stageUUID);
        return stage.getUserUUID();
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Team for Stage: {}", stageUUID);
        Stage stage = getStage(caseUUID, stageUUID);
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

    Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, String emailType, UUID transitionNoteUUID) {
        log.debug("Creating Stage of type: {}", stageType);
        Stage stage = new Stage(caseUUID, stageType, teamUUID, transitionNoteUUID);
        // Try and overwrite the deadline with inputted values from the data map.
        String overrideDeadline = caseDataService.getCaseDataField(caseUUID, String.format("%s_DEADLINE", stageType));
        if (overrideDeadline == null) {
            LocalDate dateReceived = caseDataService.getCaseDateReceived(caseUUID);
            LocalDate deadline = infoClient.getStageDeadline(stageType, dateReceived);
            stage.setDeadline(deadline);
        } else {
            LocalDate deadline = LocalDate.parse(overrideDeadline);
            stage.setDeadline(deadline);
        }
        stageRepository.save(stage);
        auditClient.createStage(stage);

        log.info("Created Stage: {}, Type: {}, Case: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, getCaseRef(caseUUID), emailType);
        return stage;
    }

    void updateStageCurrentTransitionNote(UUID caseUUID, UUID stageUUID, UUID transitionNoteUUID) {
        log.debug("Updating Transition Note for Stage: {}", stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        stage.setTransitionNote(transitionNoteUUID);
        stageRepository.save(stage);
        log.info("Set Stage Transition Note: {} ({}) for Case {}", stageUUID, transitionNoteUUID, caseUUID, value(EVENT, STAGE_TRANSITION_NOTE_UPDATED));
    }

    void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID newTeamUUID, String emailType) {
        log.debug("Updating Team: {} for Stage: {}", newTeamUUID, stageUUID);
        Stage stage = getStage(caseUUID, stageUUID);
        stage.setTeam(newTeamUUID);
        checkSendOfflineQAEmail(stage);
        stageRepository.save(stage);
        auditClient.updateStageTeam(stage);
        if (newTeamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID, value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, newTeamUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), newTeamUUID, getCaseRef(caseUUID), emailType);
        }
    }

    void checkSendOfflineQAEmail(Stage stage) {
        if (stage.getStageType().equals(Stage.DCU_DTEN_INITIAL_DRAFT) || stage.getStageType().equals(Stage.DCU_TRO_INITIAL_DRAFT) || stage.getStageType().equals(Stage.DCU_MIN_INITIAL_DRAFT)) {
            final String offlineQaUser = getOfflineQaUser(stage.getData());
            final UUID stageUserUUID = getLastCaseUserUUID(stage.getCaseUUID());
            if (offlineQaUser != null && stageUserUUID != null) {
                UUID offlineQaUserUUID = UUID.fromString(offlineQaUser);
                notifyClient.sendOfflineQaEmail(stage.getCaseUUID(), stage.getUuid(), stageUserUUID, offlineQaUserUUID, getCaseRef(stage.getCaseUUID()));
            }
        }
    }

    String getOfflineQaUser(String stageData) {
        if (stageData != null && stageData.contains(Stage.OFFLINE_QA_USER)) {
            final Map dataMap = Jackson.fromJsonString(stageData, Map.class);
            if (dataMap != null) {
                return (String)dataMap.get(Stage.OFFLINE_QA_USER);
            }
        }
        return null;
    }

    UUID getLastCaseUserUUID(UUID caseUUID) {
        List<String> auditType = new ArrayList<>();
        auditType.add(STAGE_ALLOCATED_TO_USER.name());
        final Set<GetAuditResponse> linesForCase = auditClient.getAuditLinesForCase(caseUUID, auditType);
        GetAuditResponse lastAudit = null;
        for (GetAuditResponse line : linesForCase) {
            if (line.getUserID() != null) {
                if (lastAudit == null || lastAudit.getAuditTimestamp() == null) {
                    lastAudit = line;
                } else {
                    if (lastAudit != null && line.getAuditTimestamp().isAfter(lastAudit.getAuditTimestamp())) {
                        lastAudit = line;
                    }
                }
            }
        }
        return lastAudit == null ? null : UUID.fromString(lastAudit.getUserID());
    }

    void updateStageUser(UUID caseUUID, UUID stageUUID, UUID newUserUUID) {
        log.debug("Updating User: {} for Stage: {}", newUserUUID, stageUUID);
        Stage stage = getActiveStage(caseUUID, stageUUID);
        UUID currentUserUUID = stage.getUserUUID();
        stage.setUser(newUserUUID);
        stageRepository.save(stage);
        auditClient.updateStageUser(stage);
        log.info("Updated User: {} for Stage {}", newUserUUID, stageUUID, value(EVENT, STAGE_ASSIGNED_USER));
        notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, getCaseRef(caseUUID));
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
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        } else {
            Set<Stage> stages = stageRepository.findAllActiveByTeamUUIDIn(teams);
            log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
            return stages;
        }
    }

    private Stage getStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    Set<UUID> getActiveStageCaseUUIDsForUserAndTeam(UUID userUUID, UUID teamUUID) {
        log.debug("Getting Active Stages for User in Team");
        Set<Stage> stages = stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(userUUID, teamUUID);
        log.info("Returning CaseUUIDs for Active Stages for User {} in team {}", userUUID, teamUUID, value(EVENT, USERS_TEAMS_STAGE_LIST_RETRIEVED));
        Set<UUID> caseUUIDs = new HashSet<>();
        for (Stage stage : stages) {
            caseUUIDs.add(stage.getCaseUUID());
        }
        return caseUUIDs;
    }

    Set<Stage> getActiveStagesByCaseReference(String reference) {
        log.debug("Getting Active Stages for reference: {}", reference);
        Set<Stage> stages = stageRepository.findByCaseReference(reference);
        return reduceToMostActive(stages).collect(Collectors.toSet());
    }

    Set<Stage> search(SearchRequest searchRequest) {
        log.debug("Getting Stages for Search Request");
        Set<UUID> caseUUIDs = searchClient.search(searchRequest);
        if (caseUUIDs.isEmpty()) {
            log.info("No cases - Returning 0 Stages", value(EVENT, SEARCH_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        } else {
            Set<Stage> stages = stageRepository.findAllByCaseUUIDIn(caseUUIDs);
            log.info("Returning {} Stages", stages.size(), value(EVENT, SEARCH_STAGE_LIST_RETRIEVED));
            return groupByCaseUUID(stages);
        }
    }

    private static Set<Stage> groupByCaseUUID(Set<Stage> stages) {

        // Group the stages by case UUID
        Map<UUID, List<Stage>> groupedStages = stages.stream().collect(Collectors.groupingBy(Stage :: getCaseUUID));

        // for each of the entry sets, filter out none-active stages, unless there are no active stages then use the latest stage
        return groupedStages.entrySet().stream().flatMap(s -> reduceToMostActive(s.getValue())).collect(Collectors.toSet());
    }

    private static Stream<Stage> reduceToMostActive(Set<Stage> stages) {
        return reduceToMostActive(new ArrayList<>(stages));
    }

    private static Stream<Stage> reduceToMostActive(List<Stage> stages) {
        Supplier<Stream<Stage>> stageSupplier = stages :: stream;

        // If any stages are active
        if (stageSupplier.get().anyMatch(Stage :: isActive)) {
            return stageSupplier.get().filter(Stage :: isActive);
        } else {
            // return the most recent stage.
            Optional<Stage> maxDatedStage = stageSupplier.get().max(CREATED_COMPARATOR);
            return maxDatedStage.stream();
        }
    }

    private String getCaseRef(UUID caseUUID){
        return caseDataService.getCaseRef(caseUUID);
    }
}