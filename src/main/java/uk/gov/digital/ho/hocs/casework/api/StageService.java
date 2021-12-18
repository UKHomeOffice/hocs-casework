package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.json.Jackson;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
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
    private final ActionDataDeadlineExtensionService extensionService;
    private final CaseDataTypeService caseDataTypeService;

    private static final Comparator<StageWithCaseData> CREATED_COMPARATOR = Comparator.comparing(StageWithCaseData::getCreated);

    @Autowired
    public StageService(StageRepository stageRepository,
                        UserPermissionsService userPermissionsService,
                        NotifyClient notifyClient,
                        AuditClient auditClient,
                        SearchClient searchClient,
                        InfoClient infoClient,
                        CaseDataService caseDataService,
                        StagePriorityCalculator stagePriorityCalculator,
                        DaysElapsedCalculator daysElapsedCalculator,
                        StageTagsDecorator stageTagsDecorator,
                        CaseNoteService caseNoteService,
                        ContributionsProcessor contributionsProcessor,
                        ActionDataDeadlineExtensionService extensionService,
                        CaseDataTypeService caseDataTypeService) {
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
        this.extensionService = extensionService;
        this.caseDataTypeService = caseDataTypeService;
    }

    /**
     * This method should call Active basic stage because to have a user assigned there should also be a team assigned.
     */
    public UUID getStageUser(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting User for Stage: {}", stageUUID);
        Stage stage = getActiveBasicStage(caseUUID, stageUUID);
        log.debug("Got User: {} for Stage: {}", stage.getUserUUID(), stageUUID);
        return stage.getUserUUID();
    }

    public UUID getStageTeam(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Team for Stage: {}", stageUUID);
        Stage stage = getBasicStage(caseUUID, stageUUID);
        log.info("Team: {} exists is linked to stage: {} and case: {}", stage.getTeamUUID(), stageUUID, caseUUID, stageUUID);
        return stage.getTeamUUID();
    }

    public String getStageType(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Type for Stage: {}", stageUUID);
        String stageType = getBasicStage(caseUUID, stageUUID).getStageType();
        log.debug("Got Type: {} for Stage: {}", stageType, stageUUID);
        return stageType;
    }

    StageWithCaseData getActiveStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Active Stage: {} for Case: {}", stageUUID, caseUUID);
        StageWithCaseData stage = stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Active Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    @Transactional
    Stage createStage(UUID caseUUID, String stageType, UUID teamUUID, UUID userUUID, String emailType, UUID transitionNoteUUID) {
        log.debug("Creating Stage of type: {}", stageType);
        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        CaseData caseData = caseDataService.getCase(caseUUID);
        calculateDeadlines(stage, caseData);
        stage.setUserUUID(userUUID);
        stageRepository.save(stage);
        caseDataService.updateCaseData(caseData, stage.getUuid(), Map.of(CaseworkConstants.CURRENT_STAGE, stageType));
        auditClient.createStage(stage);
        log.info("Created Stage: {}, Type: {}, Case: {}, event: {}", stage.getUuid(), stage.getStageType(), stage.getCaseUUID(), value(EVENT, STAGE_CREATED));
        notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, caseData.getReference(), emailType);
        return stage;
    }

    private void calculateDeadlines(Stage stage, CaseData caseData) {
        // Try and overwrite the deadline with inputted values from the data map.
        String overrideDeadline = caseDataService.getCaseDataField(caseData, String.format("%s_DEADLINE", stage.getStageType()));
        if (overrideDeadline == null) {
            LocalDate deadline = infoClient.getStageDeadline(stage.getStageType(), caseData.getDateReceived(), caseData.getCaseDeadline());
            stage.setDeadline(deadline);
            if (caseData.getCaseDeadlineWarning() != null) {
                LocalDate deadlineWarning = infoClient.getStageDeadlineWarning(stage.getStageType(), caseData.getDateReceived(), caseData.getCaseDeadlineWarning());
                stage.setDeadlineWarning(deadlineWarning);
            }
        }

        boolean isExtended = extensionService.hasExtensions(caseData.getUuid());
        if (isExtended) {
            LocalDate deadline = infoClient.getStageDeadlineOverridingSLA(stage.getStageType(), caseData.getDateReceived(), caseData.getCaseDeadline());
            stage.setDeadline(deadline);
            if (caseData.getCaseDeadlineWarning() != null) {
                LocalDate deadlineWarning = infoClient.getStageDeadlineWarningOverridingSLA(stage.getStageType(), caseData.getDateReceived(), caseData.getCaseDeadlineWarning());
                stage.setDeadlineWarning(deadlineWarning);
            }
        }

        if (overrideDeadline != null) {
            LocalDate deadline = LocalDate.parse(overrideDeadline);
            if (stage.getDeadline() == null || stage.getDeadline().isBefore(deadline)) {
                stage.setDeadline(deadline);
                stage.setDeadlineWarning(null);
            }
        }
    }

    public void recreateStage(UUID caseUUID, UUID stageUUID, String stageType) {
        Stage stage = getBasicStage(caseUUID, stageUUID);
        auditClient.recreateStage(stage);
        caseDataService.updateCaseData(caseUUID, stageUUID, Map.of(CaseworkConstants.CURRENT_STAGE, stageType));
        log.debug("Recreated Stage {} for Case: {}, event: {}", stageUUID, caseUUID, value(EVENT, STAGE_RECREATED));

    }

    void updateStageCurrentTransitionNote(UUID caseUUID, UUID stageUUID, UUID transitionNoteUUID) {
        log.debug("Updating Transition Note for Stage: {}", stageUUID);
        Stage stage = getActiveBasicStage(caseUUID, stageUUID);
        stage.setTransitionNoteUUID(transitionNoteUUID);
        stageRepository.save(stage);
        log.info("Set Stage Transition Note: {} ({}) for Case {}", stageUUID, transitionNoteUUID, caseUUID, value(EVENT, STAGE_TRANSITION_NOTE_UPDATED));
    }

    void updateStageTeam(UUID caseUUID, UUID stageUUID, UUID newTeamUUID, String emailType) {
        log.debug("Updating Team: {} for Stage: {}", newTeamUUID, stageUUID);
        StageWithCaseData stage = getStageWithCaseData(caseUUID, stageUUID);
        stage.setTeam(newTeamUUID);
        checkSendOfflineQAEmail(stage);
        stageRepository.save(stage);
        auditClient.updateStageTeam(stage);
        if (newTeamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stageUUID, caseUUID, value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stageUUID, newTeamUUID, caseUUID, value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(caseUUID, stage.getUuid(), newTeamUUID, stage.getCaseReference(), emailType);
        }
    }

    void checkSendOfflineQAEmail(StageWithCaseData stage) {
        if (stage.getStageType().equals(StageWithCaseData.DCU_DTEN_INITIAL_DRAFT) || stage.getStageType().equals(StageWithCaseData.DCU_TRO_INITIAL_DRAFT) || stage.getStageType().equals(StageWithCaseData.DCU_MIN_INITIAL_DRAFT)) {
            final String offlineQaUser = getOfflineQaUser(stage.getData());
            final UUID stageUserUUID = getLastCaseUserUUID(stage.getCaseUUID());
            if (offlineQaUser != null && stageUserUUID != null) {
                UUID offlineQaUserUUID = UUID.fromString(offlineQaUser);
                notifyClient.sendOfflineQaEmail(stage.getCaseUUID(), stage.getUuid(), stageUserUUID, offlineQaUserUUID, stage.getCaseReference());
            }
        }
    }

    String getOfflineQaUser(String stageData) {
        if (stageData != null && stageData.contains(StageWithCaseData.OFFLINE_QA_USER)) {
            final Map dataMap = Jackson.fromJsonString(stageData, Map.class);
            if (dataMap != null) {
                return (String) dataMap.get(StageWithCaseData.OFFLINE_QA_USER);
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
        StageWithCaseData stage = getActiveStage(caseUUID, stageUUID);
        UUID currentUserUUID = stage.getUserUUID();
        stage.setUserUUID(newUserUUID);
        stageRepository.save(stage);
        auditClient.updateStageUser(stage);
        log.info("Updated User: {} for Stage {}", newUserUUID, stageUUID, value(EVENT, STAGE_ASSIGNED_USER));
        notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, stage.getCaseReference());
    }

    Set<StageWithCaseData> getActiveStagesByCaseUUID(UUID caseUUID) {
        log.debug("Getting Active Stages for Case: {}", caseUUID);
        return stageRepository.findAllActiveByCaseUUID(caseUUID);
    }

    Set<StageWithCaseData> getActiveStagesByTeamUUID(UUID teamUUID) {
        log.debug("Getting Active Stages for Team: {}", teamUUID);
        Set<StageWithCaseData> stages = stageRepository.findAllActiveByTeamUUID(teamUUID);

        updateStages(stages);

        return stages;
    }

    void updateContributions(Set<StageWithCaseData> stage) {
        log.debug("Adding contributions data for stages");
        contributionsProcessor.processContributionsForStages(stage);
    }

    StageWithCaseData getUnassignedAndActiveStageByTeamUUID(UUID teamUUID, UUID userUUID) {
        log.debug("Getting unassigned cases for user: {} in team {}", userUUID, teamUUID);
        Set<StageWithCaseData> unassignedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(teamUUID);
        if (unassignedStages.isEmpty()) {
            log.debug("No unassigned case found for user: {} in team {}", userUUID, teamUUID);
            return null;
        }

        for (StageWithCaseData stage : unassignedStages) {
            updatePriority(stage);
            updateDaysElapsed(stage);
        }

        double prevSystemCalculatedPriority = 0;
        StageWithCaseData nextAvailableStage = unassignedStages.stream().findFirst().get();
        for (StageWithCaseData stage : unassignedStages) {
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

    Set<StageWithCaseData> getActiveStagesForUsersTeamsAndCaseType() {
        log.debug("Getting Active Stages for User");
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<String> caseTypes = userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin();

        Set<StageWithCaseData> stages = stageRepository.findAllActiveByTeamUUIDAndCaseType(teams, caseTypes);

        updateStages(stages);

        log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return stages;
    }

    Set<StageWithCaseData> getActiveUserStagesWithTeamsAndCaseType(UUID userUuid) {
        log.debug("Getting users active stage");
        Set<UUID> teams = userPermissionsService.getUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<String> caseTypes = userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin();

        Set<StageWithCaseData> stages = stageRepository.findAllActiveByUserUuidAndTeamUuidAndCaseType(userUuid, teams, caseTypes);

        updateStages(stages);

        log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return stages;
    }

    private void updateStages(Set<StageWithCaseData> stages) {
        updateContributions(stages);

        for (StageWithCaseData stage : stages) {
            updatePriority(stage);
            updateDaysElapsed(stage);
            decorateTags(stage);
        }
    }

    private StageWithCaseData getStageWithCaseData(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage With Case Data: {} for Case: {}", stageUUID, caseUUID);
        StageWithCaseData stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage With Case Data: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    private Stage getBasicStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findBasicStageByCaseUuidAndStageUuid(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    private Stage getActiveBasicStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Active Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findActiveBasicStageByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Active Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(String.format("Stage UUID: %s not found!", stageUUID), STAGE_NOT_FOUND);
        }
    }

    Set<UUID> getActiveStageCaseUUIDsForUserAndTeam(UUID userUUID, UUID teamUUID) {
        log.debug("Getting Active Stages for User in Team");
        Set<Stage> stages = stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(userUUID, teamUUID);
        log.info("Returning CaseUUIDs for Active Stages for User {} in team {}", userUUID, teamUUID, value(EVENT, USERS_TEAMS_STAGE_LIST_RETRIEVED));
        return stages.stream().map(Stage::getCaseUUID).collect(Collectors.toSet());
    }

    Set<StageWithCaseData> getActiveStagesByCaseReference(String reference) {
        log.debug("Getting Active Stages for reference: {}", reference);
        Set<StageWithCaseData> stages = stageRepository.findByCaseReference(reference);
        return reduceToMostActive(stages).collect(Collectors.toSet());
    }

    Set<StageWithCaseData> search(SearchRequest searchRequest) {
        log.debug("Getting Stages for Search Request");
        Set<UUID> caseUUIDs = searchClient.search(searchRequest);
        if (caseUUIDs.isEmpty()) {
            log.info("No cases - Returning 0 Stages", value(EVENT, SEARCH_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<StageWithCaseData> stages = stageRepository.findAllByCaseUUIDIn(caseUUIDs);

        // done like this because the case relationship is in the info schema
        // get the case types with a previous case type and reduce to
        // Map<K, V>, - K is the previousCaseType, V is the caseType
        Map<String, String> caseTypes = caseDataTypeService.getAllCaseDataTypes(caseType -> Objects.nonNull(caseType.getPreviousCaseType()))
                .collect(Collectors.toMap(CaseDataType::getPreviousCaseType, CaseDataType::getDisplayCode));

        // map the previous case type on to the cases found
        // only stages with completed cases have the next caseType
        stages.stream()
                .filter(StageWithCaseData::getCompleted)
                .forEach(stage -> stage.setNextCaseType(caseTypes.get(stage.getCaseDataType())));

        log.info("Returning {} Stages", stages.size(), value(EVENT, SEARCH_STAGE_LIST_RETRIEVED));
        return groupByCaseUUID(stages);

    }

    Set<StageWithCaseData> getAllStagesForCaseByCaseUUID(UUID caseUUID) {
        log.debug("Getting all stages for case: {}", caseUUID);
        Set<StageWithCaseData> caseStages = stageRepository.findAllByCaseUUID(caseUUID);
        if (!caseStages.isEmpty()) {
            return caseStages;
        } else {
            throw new ApplicationExceptions.EntityNotFoundException(
                    String.format("No stages found for caseUUID: %s", caseUUID), STAGES_NOT_FOUND
            );
        }
    }

    private static Set<StageWithCaseData> groupByCaseUUID(Set<? extends StageWithCaseData> stages) {

        // Group the stages by case UUID
        Map<UUID, List<StageWithCaseData>> groupedStages = stages.stream().collect(Collectors.groupingBy(StageWithCaseData::getCaseUUID));

        // for each of the entry sets, filter out none-active stages, unless there are no active stages then use the latest stage
        return groupedStages.entrySet().stream().flatMap(s -> reduceToMostActive(s.getValue())).collect(Collectors.toSet());
    }

    private static Stream<StageWithCaseData> reduceToMostActive(Set<StageWithCaseData> stages) {
        return reduceToMostActive(new ArrayList<>(stages));
    }

    private static Stream<StageWithCaseData> reduceToMostActive(List<StageWithCaseData> stages) {
        Supplier<Stream<StageWithCaseData>> stageSupplier = stages::stream;

        // If any stages are active
        if (stageSupplier.get().anyMatch(StageWithCaseData::isActive)) {
            return stageSupplier.get().filter(StageWithCaseData::isActive);
        } else {
            // return the most recent stage.
            Optional<StageWithCaseData> maxDatedStage = stageSupplier.get().max(CREATED_COMPARATOR);
            return maxDatedStage.stream();
        }
    }

    private void updatePriority(StageWithCaseData stage) {
        log.info("Updating priority for stage : {}", stage.getCaseUUID());
        stagePriorityCalculator.updatePriority(stage);
    }

    private void updateDaysElapsed(StageWithCaseData stage) {
        log.info("Updating days elapsed for stage : {}", stage.getCaseUUID());
        daysElapsedCalculator.updateDaysElapsed(stage);
    }

    private void decorateTags(StageWithCaseData stage) {
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
