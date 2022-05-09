package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.RecreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.api.dto.WithdrawCaseRequest;
import uk.gov.digital.ho.hocs.casework.application.LogEvent;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.*;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
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
    private final StagePriorityCalculator stagePriorityCalculator;
    private final DaysElapsedCalculator daysElapsedCalculator;
    private final StageTagsDecorator stageTagsDecorator;
    private final CaseNoteService caseNoteService;
    private final ContributionsProcessor contributionsProcessor;
    private final ActionDataDeadlineExtensionService extensionService;
    private final DeadlineService deadlineService;

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
                        DeadlineService deadlineService) {
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
        this.deadlineService = deadlineService;
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
    public Stage createStage(UUID caseUUID, CreateStageRequest createStageRequest) {

        log.debug("Creating Stage: {} for case {}", createStageRequest, caseUUID);

        Set<Stage> allStages = stageRepository.findAllByCaseUUIDAsStage(caseUUID);
        Set<Stage> activeStages = allStages.stream().filter(stage -> stage.getTeamUUID() != null).collect(Collectors.toSet());
        if (activeStages.size() > 1) {
            log.warn("More than 1 active stage, all active stages will be deactivated");
        }

        List<Stage> existingStageOfRequestedType = allStages.stream().filter(stage -> stage.getStageType().equals(createStageRequest.getType())).collect(Collectors.toList());

        boolean isRecreation = false;
        boolean isRecreateOfCurrentlyActive = false;
        Stage stageToActivate;
        if (existingStageOfRequestedType.isEmpty()) {
            log.debug("Stage type of {} does not currently exist for caseUUID: {}, creating new stage",createStageRequest.getType(), caseUUID);
            stageToActivate = new Stage(caseUUID, createStageRequest.getType(),
                    null, null, createStageRequest.getTransitionNoteUUID());
        } else {
            // Assumes only 1 historical stage existed.
            log.debug("Stage type of {} exists for caseUUID: {}, recreating stage",createStageRequest.getType(), caseUUID);
            stageToActivate = existingStageOfRequestedType.get(0);
            isRecreation = true;
            isRecreateOfCurrentlyActive = activeStages.contains(stageToActivate);
        }

        CaseData caseData = caseDataService.getCase(caseUUID);
        calculateDeadlines(stageToActivate, caseData);

        stageRepository.save(stageToActivate);
        log.info("Created Stage: {}, Type: {}, Case: {}, event: {}", stageToActivate.getUuid(), stageToActivate.getStageType(), stageToActivate.getCaseUUID(), value(EVENT, STAGE_CREATED));

        caseDataService.updateCaseData(caseData, stageToActivate.getUuid(), Map.of(CaseworkConstants.CURRENT_STAGE, stageToActivate.getStageType()));

        assignTeamAndMemberUserToStage(stageToActivate, createStageRequest.getTeamUUID(), createStageRequest.getUserUUID());

        List<UUID> currentUserUUIDList = activeStages.stream().map(BaseStage::getUserUUID).distinct().collect(Collectors.toList());

        // deactivate old active stage/s if new stage not current stage
        if (!isRecreateOfCurrentlyActive) {
            activeStages.forEach(value -> updateStageTeam(value, caseData.getDataMap(), caseData.getReference(), null, null));
        }

        // Update audit and timeline unless active and allocated to same user and team - positioned here maintains timeline consistency.
        if (isRecreation && !isRecreateOfCurrentlyActive) {
            auditClient.recreateStage(stageToActivate);
        } else if (!isRecreation) {
            auditClient.createStage(stageToActivate);
        }

        // Issue assignment notifications
        updateAssignmentAudit(stageToActivate);

        UUID currentUserUUID = null;

        if (!currentUserUUIDList.isEmpty()) {
            // Assumes only one user assigned to case at any one time.
            currentUserUUID = currentUserUUIDList.get(0);
        }
        sendAssignmentNotifications(caseData, stageToActivate, currentUserUUID);

        return stageToActivate;
    }

    private void assignTeamAndMemberUserToStage(Stage stage, UUID newTeamUUID, UUID newUserUUID) {

        if (newTeamUUID == null) {
            log.debug("Updating Team for Stage: {} using default team",stage.getUuid());
            TeamDto teamDto = infoClient.getTeamForStageType(stage.getStageType());
            stage.setTeam(teamDto.getUuid());
        } else {
            log.debug("Updating Team for Stage: {} with requested team: {}",stage.getUuid(),newTeamUUID);
            stage.setTeam(newTeamUUID);
        }

        if (stage.getTeamUUID() != null  && newUserUUID != null) {
            log.debug("Updating User: {} for Stage: {}",stage.getUserUUID() , stage.getUuid());

            UserDto userInTeam = infoClient.getUserForTeam(stage.getTeamUUID(), newUserUUID);
            if (userInTeam == null) {
                log.warn(
                        "Requested user {} for new stage {} is not a member of team {}; setting userUUID to null",
                        stage.getUserUUID(), stage, stage.getTeamUUID(), value(EVENT, STAGE_ASSIGNED_USER_FAILURE));
                stage.setUserUUID(null);
            } else {
                stage.setUserUUID(newUserUUID);
            }
        }

        stageRepository.save(stage);
        log.info("Event: {} Case: {}, Stage: {}, Team assigned: {}",value(EVENT, STAGE_ASSIGNED_TEAM),stage.getCaseUUID(),stage.getUuid(),stage.getTeamUUID());
    }

    private void updateAssignmentAudit(BaseStage stage) {
        auditClient.updateStageTeam(stage);
        if (stage.getUserUUID() != null) {
            auditClient.updateStageUser(stage);
        }
    }

    private void sendAssignmentNotifications(CaseData caseData, Stage stage, UUID currentUserUUID) {
        notifyClient.sendTeamEmail(stage.getCaseUUID(), stage.getUuid(), stage.getTeamUUID(), caseData.getReference(), "ALLOCATE_TEAM");

        checkSendOfflineQAEmail(stage, caseData.getDataMap(), caseData.getReference());

        if (stage.getUserUUID() != null) {
            log.info("Event: {} Case: {}, Stage: {}, User assigned: {}",value(EVENT, STAGE_ASSIGNED_USER), stage.getCaseUUID(), stage.getUuid(), stage.getUserUUID());
            notifyClient.sendUserEmail(stage.getCaseUUID(), stage.getUuid(), currentUserUUID, stage.getUserUUID(), caseData.getReference());
        }
    }

    private void calculateDeadlines(Stage stage, CaseData caseData) {
        log.debug("Updating Stage Deadline; Case: {}, Stage: {}", stage.getCaseUUID(), stage.getUuid());
        // Try and overwrite the deadline with inputted values from the data map.
        var overrideDeadline = caseData.getData(String.format("%s_DEADLINE", stage.getStageType()));

        final StageTypeDto stageDefinition = infoClient.getAllStagesForCaseType(caseData.getType()).stream()
                .filter(element -> element.getType().equals(stage.getStageType())).collect(Collectors.toList()).get(0);

        if (overrideDeadline == null) {
            LocalDate deadline =
                    deadlineService.calculateWorkingDaysForStage(
                            caseData.getType(),
                            caseData.getDateReceived(),
                            caseData.getCaseDeadline(),
                            stageDefinition.getSla());
            stage.setDeadline(deadline);
            if (caseData.getCaseDeadlineWarning() != null) {
                stage.setDeadlineWarning(caseData.getCaseDeadlineWarning());
            }
        }

        boolean isExtended = extensionService.hasExtensions(caseData.getUuid());
        if (isExtended) {
            stage.setDeadline(caseData.getCaseDeadline());
            if (caseData.getCaseDeadlineWarning() != null) {
                LocalDate deadlineWarning = caseData.getCaseDeadlineWarning();
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
        log.info("Stage Deadline Updated; Case: {}, Stage: {}", stage.getCaseUUID(), stage.getUuid());
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
        updateStageTeam(stage, stage.getData(),stage.getCaseReference(), newTeamUUID, emailType);
    }

    private void updateStageTeam(BaseStage stage, Map<String, String> caseDataMap, String caseReference, UUID newTeamUUID, String emailType) {
        stage.setTeam(newTeamUUID);
        checkSendOfflineQAEmail(stage, caseDataMap, caseReference);
        stageRepository.save(stage);
        auditClient.updateStageTeam(stage);
        if (newTeamUUID == null) {
            log.info("Completed Stage ({}) for Case {}", stage.getUuid(), stage.getCaseUUID(), value(EVENT, STAGE_COMPLETED));
        } else {
            log.info("Set Stage Team: {} ({}) for Case {}", stage.getUuid(), newTeamUUID, stage.getCaseUUID(), value(EVENT, STAGE_ASSIGNED_TEAM));
            notifyClient.sendTeamEmail(stage.getCaseUUID(), stage.getUuid(), newTeamUUID, caseReference, emailType);
        }
    }

    void checkSendOfflineQAEmail(StageWithCaseData stage) {
        checkSendOfflineQAEmail(stage, stage.getData(), stage.getCaseReference());
    }

    private void checkSendOfflineQAEmail(BaseStage stage, Map<String, String> caseDataMap, String caseReference) {
        if (stage.getStageType().equals(StageWithCaseData.DCU_DTEN_INITIAL_DRAFT) || stage.getStageType().equals(StageWithCaseData.DCU_TRO_INITIAL_DRAFT) || stage.getStageType().equals(StageWithCaseData.DCU_MIN_INITIAL_DRAFT)) {
            final String offlineQaUser = caseDataMap.get(StageWithCaseData.OFFLINE_QA_USER);
            final UUID stageUserUUID = getLastCaseUserUUID(stage.getCaseUUID());
            if (offlineQaUser != null && stageUserUUID != null) {
                UUID offlineQaUserUUID = UUID.fromString(offlineQaUser);
                notifyClient.sendOfflineQaEmail(stage.getCaseUUID(), stage.getUuid(), stageUserUUID, offlineQaUserUUID, caseReference);
            }
        }
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
        log.info("Updated User: {} for Stage {}, event: {}", newUserUUID, stageUUID, value(EVENT, STAGE_ASSIGNED_USER));
        notifyClient.sendUserEmail(caseUUID, stage.getUuid(), currentUserUUID, newUserUUID, stage.getCaseReference());
    }

    Set<StageWithCaseData> getActiveStagesByCaseUUID(UUID caseUUID) {
        log.debug("Getting Active Stages for Case: {}", caseUUID);
        return stageRepository.findAllActiveByCaseUUID(caseUUID);
    }

    Set<StageWithCaseData> getActiveStagesByTeamUUID(UUID teamUUID) {
        log.debug("Getting Active Stages for Team: {}", teamUUID);

        Set<UUID> usersTeam = userPermissionsService.getExpandedUserTeams();

        if (!usersTeam.contains(teamUUID)) {
            log.warn("User {} attempted to view team {}", userPermissionsService.getUserId(), teamUUID, value(EVENT, SECURITY_FORBIDDEN));
            throw new SecurityExceptions.ForbiddenException("User does not have access to the requested resource", SECURITY_FORBIDDEN);
        }

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
            stagePriorityCalculator.updatePriority(stage.getData(), stage.getCaseDataType());
            daysElapsedCalculator.updateDaysElapsed(stage.getData(), stage.getCaseDataType());
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

    Set<StageWithCaseData> getActiveStagesForUsersTeams() {
        log.debug("Getting active stages for users teams");

        Set<UUID> teams = userPermissionsService.getExpandedUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<StageWithCaseData> stages = stageRepository.findAllActiveByTeamUUID(teams);

        updateStages(stages);

        log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return stages;
    }

    Set<StageWithCaseData> getActiveUserStagesWithTeamsForUser(UUID userUuid) {
        log.debug("Getting active stages for teams a user has and is also assigned to");

        Set<UUID> teams = userPermissionsService.getExpandedUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<StageWithCaseData> stages = stageRepository.findAllActiveByUserUuidAndTeamUuid(userUuid, teams);

        updateStages(stages);

        log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return stages;
    }

    private void updateStages(Set<StageWithCaseData> stages) {
        updateContributions(stages);

        for (StageWithCaseData stage : stages) {
            stagePriorityCalculator.updatePriority(stage.getData(), stage.getCaseDataType());
            daysElapsedCalculator.updateDaysElapsed(stage.getData(), stage.getCaseDataType());
            stage.setTag(stageTagsDecorator.decorateTags(stage.getData(), stage.getStageType()));
        }
    }

    private StageWithCaseData getStageWithCaseData(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage With Case Data: {} for Case: {}", stageUUID, caseUUID);
        StageWithCaseData stage = stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage With Case Data: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            String msg = String.format("Stage UUID: %s for Case: %s not found!", stageUUID, caseUUID);
            log.error(msg);
            throw new ApplicationExceptions.EntityNotFoundException(msg, STAGE_NOT_FOUND);
        }
    }

    private Stage getBasicStage(UUID caseUUID, UUID stageUUID) {
        log.debug("Getting Stage: {} for Case: {}", stageUUID, caseUUID);
        Stage stage = stageRepository.findBasicStageByCaseUuidAndStageUuid(caseUUID, stageUUID);
        if (stage != null) {
            log.info("Got Stage: {} for Case: {}", stageUUID, caseUUID);
            return stage;
        } else {
            String msg = String.format("Stage UUID: %s for Case: %s not found!", stageUUID, caseUUID);
            log.error(msg);
            throw new ApplicationExceptions.EntityNotFoundException(msg, STAGE_NOT_FOUND);
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
        Map<String, String> caseTypes = infoClient.getAllCaseTypes()
                .stream()
                .filter( caseType -> Objects.nonNull(caseType.getPreviousCaseType()))
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

    public void withdrawCase(UUID caseUUID, UUID stageUUID, WithdrawCaseRequest request) {
        log.info("About to withdraw case : {}", caseUUID);
        CaseData caseData = caseDataService.getCase(caseUUID);

        for(ActiveStage activeStage : caseData.getActiveStages()){
            updateStageTeam(caseUUID, activeStage.getUuid(), null, null );
        }

        Map<String, String> data = new HashMap<>();
        data.put("Withdrawn", "True");
        data.put("WithdrawalDate", request.getWithdrawalDate());
        data.put("WithdrawalReason", request.getNotes());
        data.put(CaseworkConstants.CURRENT_STAGE, "");

        caseDataService.updateCaseData(caseUUID, stageUUID, data);
        caseDataService.completeCase(caseUUID, true);

        caseNoteService.createCaseNote(caseUUID,"WITHDRAW", request.getNotes());

        log.info("Case withdraw completed : {}", caseUUID, value(EVENT, CASE_WITHDRAWN));
    }
}
