package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseDataTag;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.repository.WorkstackStageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class WorkstackServiceTest {

    @Mock
    private UserPermissionsService userPermissionsService;

    @Mock
    private WorkstackStageRepository workstackRepository;

    @Mock
    private StageService stageService;

    @Mock
    private ContributionsProcessor contributionsProcessor;

    @Mock
    private StagePriorityCalculator stagePriorityCalculator;

    @Mock
    private DaysElapsedCalculator daysElapsedCalculator;

    @Mock
    private StageTagsDecorator stageTagsDecorator;

    private WorkstackService workstackService;

    private final UUID transitionNoteUUID = UUID.randomUUID();

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID teamUUID = UUID.randomUUID();

    private final UUID userUUID = UUID.randomUUID();

    private SomuItem somuItem;

    private CaseData caseData;

    private ActiveStage activeStage;

    private LocalDateTime caseCreated = LocalDateTime.of(2022, 11, 1, 0, 0);

    private LocalDate dateReveived = LocalDate.of(2022, 11, 1);

    @Before
    public void setUp() {
        somuItem = new SomuItem(UUID.randomUUID(), caseUUID, UUID.randomUUID(),
            "{ \"contributionDueDate\" : \"0000-12-31\"}");

        caseData = new CaseData(caseUUID, caseCreated, "COMP", "COMP/123456/22", false, Map.of(), null, null, null,
            null, Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(),
            Set.of(somuItem));

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "COMP_SERVICE_TRIAGE", null, null,
            transitionNoteUUID, caseUUID, teamUUID, userUUID, caseData, null, null, null);

        caseData.setActiveStages(Set.of(activeStage));

        workstackService = new WorkstackService(workstackRepository, userPermissionsService, stagePriorityCalculator,
            daysElapsedCalculator, stageTagsDecorator, contributionsProcessor, stageService);

    }

    @Test
    public void shouldGetActiveStages_blankResult() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        workstackService.getActiveStagesForUsersTeams();

        verify(workstackRepository).findAllActiveByTeamUUID(teams);

        verifyNoMoreInteractions(workstackRepository);
    }

    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(workstackRepository.findAllActiveByTeamUUID(teams)).thenReturn(Stream.of(caseData));

        workstackService.getActiveStagesForUsersTeams();

        verify(userPermissionsService).getExpandedUserTeams();
        verify(workstackRepository).findAllActiveByTeamUUID(teams);
        verify(stagePriorityCalculator).updatePriority(caseData, caseData.getType());
        verify(daysElapsedCalculator).updateDaysElapsed(caseData.getDataMap(), caseData.getType());

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveStagesEmpty() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        workstackService.getActiveStagesForUsersTeams();

        // We don't try and get active stages with no teams (empty set) because we're going to get 0 results.
        verify(userPermissionsService).getExpandedUserTeams();
        checkNoMoreInteraction();

    }

    @Test
    public void shouldGetUnassignedAndActiveStageByTeamUUID() {
        workstackService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID);

        verify(workstackRepository).findAllUnassignedAndActiveByTeamUUID(teamUUID);

        verifyNoMoreInteractions(workstackRepository);
    }

    @Test
    public void shouldGetActiveStagesByTeamUuids() {
        Set<UUID> teamUuids = Set.of(teamUUID);

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teamUuids);
        when(workstackRepository.findAllActiveByTeamUUID(teamUUID)).thenReturn(Stream.of(caseData));

        workstackService.getActiveStagesByTeamUUID(teamUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(contributionsProcessor).processContributionsForCase(caseData);
        verify(workstackRepository).findAllActiveByTeamUUID(teamUUID);
    }

    @Test(expected = SecurityExceptions.ForbiddenException.class)
    public void shouldGetActiveStagesByTeamUuids_ForbiddenThrownWhenNotInTeam() {
        Set<UUID> teamUuids = Set.of(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teamUuids);

        workstackService.getActiveStagesByTeamUUID(teamUUID);
    }

    @Test
    public void shouldGetActiveUserStagesWithTeams() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        caseData = new CaseData(caseUUID, caseCreated, "MIN", "MIN/123456/22", false, Map.of(), null, null, null, null,
            Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(), Set.of(somuItem));

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null,
            transitionNoteUUID, caseUUID, teamUUID, userUUID, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(workstackRepository.findAllActiveByUserUuidAndTeamUuid(userUUID, teams)).thenReturn(Set.of(caseData));

        workstackService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(workstackRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);
        verify(stagePriorityCalculator).updatePriority(caseData, caseData.getType());
        verify(daysElapsedCalculator).updateDaysElapsed(caseData.getDataMap(), caseData.getType());

        checkNoMoreInteraction();
    }

    @Test
    public void shouldSetTagsOnStages() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        caseData = new CaseData(caseUUID, caseCreated, "MIN", "MIN/123456/22", false, new HashMap<>(), null, null, null,
            null, Collections.emptySet(), null, null, dateReveived, false, null, new HashSet<>(), Set.of());

        activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null,
            transitionNoteUUID, caseUUID, teamUUID, userUUID, caseData, null, null, null);
        caseData.setActiveStages(Set.of(activeStage));

        ArrayList<String> tags = new ArrayList<>(Collections.singleton("HS"));

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(workstackRepository.findAllActiveByUserUuidAndTeamUuid(userUUID, teams)).thenReturn(Set.of(caseData));
        when(stageTagsDecorator.decorateTags(caseData.getDataMap(), activeStage.getStageType())).thenReturn(tags);

        var result = workstackService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(workstackRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);
        verify(stagePriorityCalculator).updatePriority(caseData, caseData.getType());
        verify(daysElapsedCalculator).updateDaysElapsed(caseData.getDataMap(), caseData.getType());
        verify(stageTagsDecorator).decorateTags(caseData.getDataMap(), activeStage.getStageType());

        // assertThat(result).extracting(ActiveStage::getCaseData).extracting(CaseData::getTag).containsOnly(Set.of(new CaseDataTag(caseUUID, "HS")));

        assertThat(result.iterator().next().getCaseData().getTag()).containsOnly(new CaseDataTag(caseUUID, "HS"));

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveUserStagesWithTeamsAndCaseType_noTeams() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        workstackService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        checkNoMoreInteraction();
    }

    @Test
    public void getActiveUserStagesWithTeamsAndCaseType_blankResult() {
        Set<UUID> teams = Set.of(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        workstackService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(workstackRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);

        verifyNoMoreInteractions(workstackRepository);
    }

    @Test
    public void getActiveUserStagesWithTeamsAndCaseType_unworkableCases() {
        Set<UUID> teams = Set.of(UUID.randomUUID());

        var unworkableCaseUUID = UUID.randomUUID();
        var workableCaseUUID = UUID.randomUUID();

        var workableCaseData = new CaseData(workableCaseUUID, caseCreated, "MIN", "MIN/123456/22", false, Map.of(),
            null, null, null, null, Collections.emptySet(), null, null, dateReveived, false, null,
            Collections.emptySet(), Set.of());

        var workableActiveStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null, null,
            transitionNoteUUID, workableCaseUUID, teamUUID, userUUID, workableCaseData, null, null, null);
        workableCaseData.setActiveStages(Set.of(workableActiveStage));

        var unworkableCaseData = new CaseData(unworkableCaseUUID, caseCreated, "MIN", "MIN/123456/22", false,
            Map.of("Unworkable", "true"), null, null, null, null, Collections.emptySet(), null, null, dateReveived,
            false, null, Collections.emptySet(), Set.of());

        var unworkableActiveStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "DCU_MIN_MARKUP", null,
            null, transitionNoteUUID, unworkableCaseUUID, teamUUID, userUUID, unworkableCaseData, null, null, null);
        unworkableCaseData.setActiveStages(Set.of(unworkableActiveStage));

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        when(workstackRepository.findAllActiveByUserUuidAndTeamUuid(userUUID, teams)).thenReturn(
            Set.of(workableCaseData, unworkableCaseData));

        var result = workstackService.getActiveUserStagesWithTeamsForUser(userUUID);

        assertThat(result).extracting(ActiveStage::getCaseUUID).containsOnly(workableCaseUUID);

        verify(workstackRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);

        verifyNoMoreInteractions(workstackRepository);
    }

    private void checkNoMoreInteraction() {
        verifyNoMoreInteractions(workstackRepository, userPermissionsService, stagePriorityCalculator,
            daysElapsedCalculator);
    }

}