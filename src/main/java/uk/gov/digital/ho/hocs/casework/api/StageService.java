package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.json.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.WithdrawCaseRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_WITHDRAWN;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SEARCH_STAGE_LIST_EMPTY;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SEARCH_STAGE_LIST_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGES_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_ASSIGNED_TEAM;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_ASSIGNED_USER;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_COMPLETED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_RECREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.STAGE_TRANSITION_NOTE_UPDATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.TEAMS_STAGE_LIST_EMPTY;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.TEAMS_STAGE_LIST_RETRIEVED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.USERS_TEAMS_STAGE_LIST_RETRIEVED;
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
    private final StagePriorityCalculator stagePriorityCalculator;
    private final DaysElapsedCalculator daysElapsedCalculator;
    private final StageTagsDecorator stageTagsDecorator;
    private final CaseNoteService caseNoteService;
    private final ContributionsProcessor contributionsProcessor;

    private static final Comparator<Stage> CREATED_COMPARATOR = Comparator.comparing(Stage::getCreated);


    @Autowired
    public StageService(StageRepository stageRepository, UserPermissionsService userPermissionsService, NotifyClient notifyClient, AuditClient auditClient, SearchClient searchClient, InfoClient infoClient,
                        @Qualifier("CaseDataService") CaseDataService caseDataService, StagePriorityCalculator stagePriorityCalculator,
                        DaysElapsedCalculator daysElapsedCalculator, StageTagsDecorator stageTagsDecorator, CaseNoteService caseNoteService, ContributionsProcessor contributionsProcessor) {
        this.stageRepository = stageRepository;
        this.userPermissionsService = userPermissionsService;
        this.notifyClient = notifyClient;
        this.auditClient = auditClient;
        this.searchClient = searchClient;
        this.infoClient = infoClient;
        this.caseDataService = caseDataService;
        this.stagePriorityCalculator = stagePriorityCalculator;
        this.daysElapsedCalculator = daysElapsedCalculator;
        this.stageTagsDecorator = stageTagsDecorator;
        this.caseNoteService = caseNoteService;
        this.contributionsProcessor = contributionsProcessor;
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

    public String getStageTypeFromStageData(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Type for Stage: {}", stageUUID);
        String stageType = getStage(caseUUID, stageUUID).getStageType();
        log.debug("Got Type: {} for Stage: {}", stageType, stageUUID);
        return stageType;
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

    Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, String emailType, UUID transitionNoteUUID) {
        log.debug("Creating Stage of type: {}", stageType);
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        // Try and overwrite the deadline with inputted values from the data map.
        String overrideDeadline = caseDataService.getCaseDataField(caseUUID, String.format("%s_DEADLINE", stageType));
        if (overrideDeadline == null) {
            CaseData caseData = caseDataService.getCase(caseUUID);
            LocalDate deadline = infoClient.getStageDeadline(stageType, caseData.getDateReceived(), caseData.getCaseDeadline());
            stage.setDeadline(deadline);
            if (caseData.getCaseDeadlineWarning() != null) {
                LocalDate deadlineWarning = infoClient.getStageDeadlineWarning(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning());
                stage.setDeadlineWarning(deadlineWarning);
            }
        } else {
            LocalDate deadline = LocalDate.parse(overrideDeadline);
            stage.setDeadline(deadline);
            stage.setDeadlineWarning(null);
        }
        stage.setUser(userUUID);
        stageRepository.save(stage);
        auditClient.createStage(stage);
        updateCurrentStageForCase(caseUUID, stage.getUuid(), stageType);
        log.info("Created Stage: {}, Type: {}, Case: {}, event: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, getCaseRef(caseUUID), emailType);
        return stage;
    }

    public void recreateStage(UUID caseUUID, UUID stageUUID, String stageType) {
        Stage stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        auditClient.recreateStage(stage);
        updateCurrentStageForCase(caseUUID, stageUUID, stageType);
        log.debug("Recreated Stage {} for Case: {}, event: {}", stageUUID, caseUUID, value(EVENT, STAGE_RECREATED));

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
                return (String) dataMap.get(Stage.OFFLINE_QA_USER);
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
                    if (line.getAuditTimestamp().isAfter(lastAudit.getAuditTimestamp())) {
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
        Set<Stage> stages = stageRepository.findAllActiveByTeamUUID(teamUUID);

        for (Stage stage : stages) {
            updateContributions(stage);
            updatePriority(stage);
            updateDaysElapsed(stage);
            decorateTags(stage);
        }

        return stages;
    }

    void updateContributions(Stage stage) {
        log.debug("Adding contributions data for stage : {}", stage.getCaseUUID());
        contributionsProcessor.processContributionsForStage(stage);
    }

    Stage getUnassignedAndActiveStageByTeamUUID(UUID teamUUID, UUID userUUID) {
        log.debug("Getting unassigned cases for user: {} in team {}", userUUID, teamUUID);
        Set<Stage> unassignedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(teamUUID);
        if (unassignedStages.isEmpty()) {
            log.debug("No unassigned case found for user: {} in team {}", userUUID, teamUUID);
            return null;
        }

        for (Stage stage : unassignedStages) {
            updatePriority(stage);
            updateDaysElapsed(stage);
        }

        double prevSystemCalculatedPriority = 0;
        Stage nextAvailableStage = unassignedStages.stream().findFirst().get();
        for (Stage stage : unassignedStages) {
            JSONObject caseData = new JSONObject(stage.getData());
            double systemCalculatedPriority = caseData.getDouble("systemCalculatedPriority");

            if (systemCalculatedPriority > prevSystemCalculatedPriority) {
                prevSystemCalculatedPriority = systemCalculatedPriority;
                nextAvailableStage = stage;
            }
        }

        UUID caseUUID = nextAvailableStage.getCaseUUID();
        UUID stageUUID = nextAvailableStage.getUuid();
        updateStageUser(caseUUID, stageUUID, userUUID);

        return nextAvailableStage;
    }

    Set<Stage> getActiveStagesForUser() {
        log.debug("Getting Active Stages for User");
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        } else {
            Set<String> caseTypes = userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin();
            if (caseTypes.isEmpty()) {
                caseTypes.add("");
            }
            Set<Stage> stages = stageRepository.findAllActiveByTeamUUIDAndCaseType(teams, caseTypes);

            for (Stage stage : stages) {
                updatePriority(stage);
                updateDaysElapsed(stage);
                decorateTags(stage);
            }

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
        }

        Set<Stage> stages = stageRepository.findAllByCaseUUIDIn(caseUUIDs);

        // done like this because the case relationship is in the info schema
        // get the case types with a previous case type and reduce to
        // Map<K, V>, - K is the previousCaseType, V is the caseType
        Map<String, String> caseTypes = infoClient.getAllCaseTypes()
                .stream()
                .filter( caseType -> Objects.nonNull(caseType.getPreviousCaseType()))
                .collect(Collectors.toMap(CaseDataType::getPreviousCaseType, CaseDataType::getDisplayCode));

        // map the previous case type on to the cases found
        // only stages with completed cases have the next caseType
        stages.stream()
                .filter(stage -> stage.getCompleted())
                .forEach(stage -> stage.setNextCaseType(caseTypes.get(stage.getCaseDataType())));

        log.info("Returning {} Stages", stages.size(), value(EVENT, SEARCH_STAGE_LIST_RETRIEVED));
        return groupByCaseUUID(stages);

    }

    Set<Stage> getAllStagesForCaseByCaseUUID(UUID caseUUID) {
        log.debug("Getting all stages for case: {}", caseUUID);
        Set<Stage> caseStages = stageRepository.findAllByCaseUUID(caseUUID);
        if (!caseStages.isEmpty()) {
            return caseStages;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(
                    String.format("No stages found for caseUUID: %s", caseUUID), STAGES_NOT_FOUND
            );
        }
    }

    private static Set<Stage> groupByCaseUUID(Set<? extends Stage> stages) {

        // Group the stages by case UUID
        Map<UUID, List<Stage>> groupedStages = stages.stream().collect(Collectors.groupingBy(Stage::getCaseUUID));

        // for each of the entry sets, filter out none-active stages, unless there are no active stages then use the latest stage
        return groupedStages.entrySet().stream().flatMap(s -> reduceToMostActive(s.getValue())).collect(Collectors.toSet());
    }

    private static Stream<Stage> reduceToMostActive(Set<Stage> stages) {
        return reduceToMostActive(new ArrayList<>(stages));
    }

    private static Stream<Stage> reduceToMostActive(List<Stage> stages) {
        Supplier<Stream<Stage>> stageSupplier = stages::stream;

        // If any stages are active
        if (stageSupplier.get().anyMatch(Stage::isActive)) {
            return stageSupplier.get().filter(Stage::isActive);
        } else {
            // return the most recent stage.
            Optional<Stage> maxDatedStage = stageSupplier.get().max(CREATED_COMPARATOR);
            return maxDatedStage.stream();
        }
    }

    private String getCaseRef(UUID caseUUID) {
        return caseDataService.getCaseRef(caseUUID);
    }

    private void updateCurrentStageForCase(UUID caseUUID, UUID stageUUID, String stageType) {
        caseDataService.updateCaseData(caseUUID, stageUUID, Map.of(CaseworkConstants.CURRENT_STAGE, stageType));
    }

    private void updatePriority(Stage stage) {
        log.info("Updating priority for stage : {}", stage.getCaseUUID());
        stagePriorityCalculator.updatePriority(stage);
    }

    private void updateDaysElapsed(Stage stage) {
        log.info("Updating days elapsed for stage : {}", stage.getCaseUUID());
        daysElapsedCalculator.updateDaysElapsed(stage);
    }

    private void decorateTags(Stage stage) {
        log.info("Updating tags for stage: {}", stage.getCaseUUID());
        stageTagsDecorator.decorateTags(stage);
    }

    public void withdrawCase(UUID caseUUID, UUID stageUUID, WithdrawCaseRequest request) {
        log.info("About to withdraw case : {}", caseUUID);
        CaseData caseData = caseDataService.getCase(caseUUID);

        for(ActiveStage activeStage : caseData.getActiveStages()){
            updateStageTeam(caseUUID, activeStage.getUuid(), null, null );
        }

        Map<String, String> data = new HashMap<>();
        data.put("Withdrawn", "True");
        data.put("WithdrawalDate", request.getWithdrawalDate());
        data.put(CaseworkConstants.CURRENT_STAGE, "");

        caseDataService.updateCaseData(caseUUID, stageUUID, data);
        caseDataService.completeCase(caseUUID, true);

        caseNoteService.createCaseNote(caseUUID,"WITHDRAW", request.getNotes());

        log.info("Case withdraw completed : {}", caseUUID, value(EVENT, CASE_WITHDRAWN));
    }
}
