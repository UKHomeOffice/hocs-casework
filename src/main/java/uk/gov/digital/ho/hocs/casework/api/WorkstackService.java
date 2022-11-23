package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.WorkstackStageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.SECURITY_FORBIDDEN;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.TEAMS_STAGE_LIST_EMPTY;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.TEAMS_STAGE_LIST_RETRIEVED;

@Slf4j
@Service
public class WorkstackService {

    private final WorkstackStageRepository workstackRepository;

    private final UserPermissionsService userPermissionsService;

    private final StagePriorityCalculator stagePriorityCalculator;

    private final DaysElapsedCalculator daysElapsedCalculator;

    private final StageTagsDecorator stageTagsDecorator;

    private final ContributionsProcessor contributionsProcessor;

    private final StageService stageService;

    public WorkstackService(WorkstackStageRepository workstackRepository,
                            UserPermissionsService userPermissionsService,
                            StagePriorityCalculator stagePriorityCalculator,
                            DaysElapsedCalculator daysElapsedCalculator,
                            StageTagsDecorator stageTagsDecorator,
                            ContributionsProcessor contributionsProcessor,
                            StageService stageService) {
        this.stageService = stageService;
        this.workstackRepository = workstackRepository;
        this.userPermissionsService = userPermissionsService;
        this.stagePriorityCalculator = stagePriorityCalculator;
        this.daysElapsedCalculator = daysElapsedCalculator;
        this.stageTagsDecorator = stageTagsDecorator;
        this.contributionsProcessor = contributionsProcessor;
    }

    Set<ActiveStage> getActiveStagesByTeamUUID(UUID teamUUID) {
        log.debug("Getting Active Stages for Team: {}", teamUUID);

        Set<UUID> usersTeam = userPermissionsService.getExpandedUserTeams();

        if (!usersTeam.contains(teamUUID)) {
            log.warn("User {} attempted to view team {}", userPermissionsService.getUserId(), teamUUID,
                value(EVENT, SECURITY_FORBIDDEN));
            throw new SecurityExceptions.ForbiddenException("User does not have access to the requested resource",
                SECURITY_FORBIDDEN);
        }

        Set<CaseData> cases = workstackRepository.findAllActiveByTeamUUID(teamUUID);

        updateStages(cases);

        return cases.stream().flatMap(caseData -> caseData.getActiveStages().stream()).collect(Collectors.toSet());
    }

    void updateContributions(Set<CaseData> cases) {
        log.debug("Adding contributions data for stages");
        contributionsProcessor.processContributionsForStages(cases);
    }

    ActiveStage getUnassignedAndActiveStageByTeamUUID(UUID teamUUID, UUID userUUID) {
        log.debug("Getting unassigned cases for user: {} in team {}", userUUID, teamUUID);
        Set<CaseData> unassignedCases = workstackRepository.findAllUnassignedAndActiveByTeamUUID(teamUUID);
        if (unassignedCases.isEmpty()) {
            log.debug("No unassigned case found for user: {} in team {}", userUUID, teamUUID);
            return null;
        }

        for (CaseData caseData : unassignedCases) {
            stagePriorityCalculator.updatePriority(caseData, caseData.getType());
            daysElapsedCalculator.updateDaysElapsed(caseData.getDataMap(), caseData.getType());
        }

        double prevSystemCalculatedPriority = 0;
        ActiveStage nextAvailableStage = unassignedCases.stream().findFirst().get().getActiveStages().stream().findFirst().get();
        for (ActiveStage stage : unassignedCases.stream().flatMap(
            caseData -> caseData.getActiveStages().stream()).toList()) {
            double systemCalculatedPriority = 0.0;

            try {
                systemCalculatedPriority = Double.parseDouble(stage.getCaseData().getData("systemCalculatedPriority"));
            } catch (NumberFormatException e) {
                log.error("Error parsing systemCalculatedPriority for case: {}", stage.getCaseData().getUuid());
            }

            if (systemCalculatedPriority > prevSystemCalculatedPriority) {
                prevSystemCalculatedPriority = systemCalculatedPriority;
                nextAvailableStage = stage;
            }
        }

        UUID caseUUID = nextAvailableStage.getCaseUUID();
        UUID stageUUID = nextAvailableStage.getUuid();
        stageService.updateStageUser(caseUUID, stageUUID, userUUID);

        return nextAvailableStage;
    }

    Set<ActiveStage> getActiveStagesForUsersTeams() {
        log.debug("Getting active stages for users teams");

        Set<UUID> teams = userPermissionsService.getExpandedUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<CaseData> cases = workstackRepository.findAllActiveByTeamUUID(teams);

        updateStages(cases);

        log.info("Returning {} Stages", cases.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return cases.stream().flatMap(caseData -> caseData.getActiveStages().stream()).collect(Collectors.toSet());
    }

    Set<ActiveStage> getActiveUserStagesWithTeamsForUser(UUID userUuid) {
        log.debug("Getting active stages for teams a user has and is also assigned to");

        Set<UUID> teams = userPermissionsService.getExpandedUserTeams();
        if (teams.isEmpty()) {
            log.warn("No teams - Returning 0 Stages", value(EVENT, TEAMS_STAGE_LIST_EMPTY));
            return new HashSet<>(0);
        }

        Set<CaseData> stages = workstackRepository.findAllActiveByUserUuidAndTeamUuid(userUuid, teams).stream().filter(
            c -> !Optional.ofNullable(c.getData("Unworkable")).orElse("").equalsIgnoreCase("true")).collect(
            Collectors.toSet());

        updateStages(stages);

        log.info("Returning {} Stages", stages.size(), value(EVENT, TEAMS_STAGE_LIST_RETRIEVED));
        return stages.stream().flatMap(caseData -> caseData.getActiveStages().stream()).collect(Collectors.toSet());
    }

    private void updateStages(Set<CaseData> cases) {
        updateContributions(cases);

        for (CaseData caseData : cases) {
            stagePriorityCalculator.updatePriority(caseData, caseData.getType());
            daysElapsedCalculator.updateDaysElapsed(caseData.getDataMap(), caseData.getType());

            //TODO: HOCS-5871 Remove after Workflow Implementation released
            caseData.getActiveStages().forEach(stage -> {
                caseData.appendTags(stageTagsDecorator.decorateTags(caseData.getDataMap(), stage.getStageType()));
            });
        }
    }

}
