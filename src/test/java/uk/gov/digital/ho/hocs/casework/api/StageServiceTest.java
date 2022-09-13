package uk.gov.digital.ho.hocs.casework.api;

import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.contributions.ContributionsProcessor;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("local")
public class StageServiceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID teamUUID = UUID.randomUUID();

    private final UUID userUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    private final String stageType = "DCU_MIN_MARKUP";

    private final String allocationType = "ALLOCATE_TEAM";

    private final UUID transitionNoteUUID = UUID.randomUUID();

    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN", null, 20, 15);

    private final List<CaseDataType> caseDataTypes = List.of(CaseDataTypeFactory.from("NXT", "a5", "MIN"),
        // NXT can be reached through MIN
        caseDataType);

    private final String userID = UUID.randomUUID().toString();

    private StageService stageService;

    private final StageTypeDto stageTypeDto = new StageTypeDto("DCU_MIN_MARKUP", null, "DCU_MIN_MARKUP", 20, 15, 1);

    @Mock
    private StageRepository stageRepository;

    @Mock
    private UserPermissionsService userPermissionsService;

    @Mock
    private NotifyClient notifyClient;

    @Mock
    private AuditClient auditClient;

    @Mock
    private SearchClient searchClient;

    @Mock
    private InfoClient infoClient;

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private StagePriorityCalculator stagePriorityCalculator;

    @Mock
    private DaysElapsedCalculator daysElapsedCalculator;

    @Mock
    private StageTagsDecorator stageTagsDecorator;

    @Mock
    private CaseNoteService caseNoteService;

    @Mock
    private ContributionsProcessor contributionsProcessor;

    @Mock
    private DeadlineService deadlineService;

    @Mock
    private ActionDataDeadlineExtensionService extensionService;

    private String ALLOCATION_TYPE = "ALLOCATION_TYPE";

    private final Set<Stage> MOCK_STAGE_LIST = new HashSet<>();

    private final UUID CASE_UUID = UUID.fromString("5a2e121f-f0c8-4725-870d-c8134a0f1e6b");

    private final Stage stageDataInput = new Stage(CASE_UUID, "DATA_INPUT", null, null, null);

    private final Stage stageTriage = new Stage(CASE_UUID, "TRIAGE", null, null, null);

    private final Stage stageDraft = new Stage(CASE_UUID, "DRAFT", null, null, null);

    @Before
    public void setUp() {
        when(infoClient.getAllStagesForCaseType(caseDataType.getDisplayName())).thenReturn(Set.of(stageTypeDto));

        this.stageService = new StageService(stageRepository, userPermissionsService, notifyClient, auditClient,
            searchClient, infoClient, caseDataService, stagePriorityCalculator, daysElapsedCalculator,
            stageTagsDecorator, caseNoteService, contributionsProcessor, extensionService, deadlineService);

        MOCK_STAGE_LIST.clear();
        MOCK_STAGE_LIST.add(stageDraft);
        MOCK_STAGE_LIST.add(stageTriage);
        MOCK_STAGE_LIST.add(stageDataInput);
    }

    @Test
    public void testShouldCreateStageWithDefaultStageTeamAndNoUserOverride() {
        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getAllStagesForCaseType(caseData.getType())).thenReturn(Set.of(stageTypeDto));
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(false);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // THEN
        // -- Always called
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(infoClient).getAllStagesForCaseType(caseData.getType());
        verify(deadlineService).calculateWorkingDaysForStage("MIN", received, deadline, stageTypeDto.getSla());
        verify(extensionService).hasExtensions(caseData.getUuid());
        verify(stageRepository, times(3)).save(any(Stage.class));
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());
        verify(infoClient).getTeamForStageType(stageType);
        verify(stageRepository).save(mockExistingStage);
        verify(auditClient).updateStageTeam(mockExistingStage);

        // -- Should be called
        verify(infoClient).getTeamForStageType(stageType);
        verify(auditClient).createStage(any(Stage.class));
        verify(auditClient, times(2)).updateStageTeam(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), any(UUID.class), eq(teamUUID), any(),
            eq(allocationType.toString()));

        // -- Should NOT be called
        verify(infoClient, times(0)).getUserForTeam(any(), any());
        verify(auditClient, times(0)).updateStageUser(any(Stage.class));
        verify(notifyClient, times(0)).sendUserEmail(eq(caseData.getUuid()), any(UUID.class), any(UUID.class),
            any(UUID.class), anyString());

        verifyNoMoreInteractions(stageRepository, notifyClient, auditClient, infoClient);
    }

    @Test
    public void testShouldCreateStageWithTeamOverride() {
        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        CreateStageRequest request = new CreateStageRequest(stageType, null, teamUUID, allocationType,
            transitionNoteUUID, null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getAllStagesForCaseType(caseData.getType())).thenReturn(Set.of(stageTypeDto));
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(false);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // THEN
        // -- Always called
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(infoClient).getAllStagesForCaseType(caseData.getType());
        verify(deadlineService).calculateWorkingDaysForStage("MIN", received, deadline, stageTypeDto.getSla());
        verify(extensionService).hasExtensions(caseData.getUuid());
        verify(stageRepository, times(3)).save(any(Stage.class));
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());
        verify(stageRepository).save(mockExistingStage);
        verify(auditClient).updateStageTeam(mockExistingStage);

        // -- Should be called
        verify(auditClient).createStage(any(Stage.class));
        verify(auditClient, times(2)).updateStageTeam(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), any(UUID.class), eq(teamUUID), any(),
            eq(allocationType));

        // -- Should NOT be called
        verify(infoClient, times(0)).getTeamForStageType(stageType);
        verify(infoClient, times(0)).getUserForTeam(any(), any());
        verify(auditClient, times(0)).updateStageUser(any(Stage.class));
        verify(notifyClient, times(0)).sendUserEmail(eq(caseData.getUuid()), any(UUID.class), any(UUID.class),
            any(UUID.class), anyString());

        verifyNoMoreInteractions(stageRepository, notifyClient, auditClient, infoClient);
    }

    @Test
    public void testShouldCreateStageWithUserOverride() {
        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);
        UserDto userDto = new UserDto(userUUID.toString(), null, null, null, null);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            userUUID);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(infoClient.getAllStagesForCaseType(caseData.getType())).thenReturn(Set.of(stageTypeDto));
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(false);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        when(infoClient.getUserForTeam(teamUUID, userUUID)).thenReturn(userDto);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // THEN
        // -- Always called
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(infoClient).getAllStagesForCaseType(caseData.getType());
        verify(deadlineService).calculateWorkingDaysForStage(caseData.getType(), received, deadline,
            stageTypeDto.getSla());
        verify(extensionService).hasExtensions(caseData.getUuid());
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());
        verify(stageRepository, times(3)).save(any(Stage.class));
        verify(stageRepository).save(mockExistingStage);
        verify(auditClient).updateStageTeam(mockExistingStage);

        // -- Should be called
        verify(infoClient).getTeamForStageType(stageType);
        verify(auditClient).createStage(any(Stage.class));
        verify(auditClient, times(2)).updateStageTeam(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), any(UUID.class), eq(teamUUID), any(),
            eq(allocationType));
        verify(infoClient).getUserForTeam(teamUUID, userUUID);
        verify(auditClient).updateStageUser(any(Stage.class));
        verify(notifyClient).sendUserEmail(eq(caseData.getUuid()), any(UUID.class), any(UUID.class), eq(userUUID),
            anyString());

        // -- Should NOT be called

        verifyNoMoreInteractions(stageRepository, notifyClient, auditClient, infoClient, caseDataService,
            deadlineService, extensionService);
    }

    @Test
    public void testShouldCreateStageDisregardingUserOverrideWhenUserNotInTeam() {
        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            userUUID);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(false);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        when(infoClient.getUserForTeam(teamUUID, userUUID)).thenReturn(null);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // -- Always called
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(infoClient).getAllStagesForCaseType(caseData.getType());
        verify(deadlineService).calculateWorkingDaysForStage(caseData.getType(), received, deadline,
            stageTypeDto.getSla());
        verify(extensionService).hasExtensions(caseData.getUuid());
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());
        verify(stageRepository, times(3)).save(any(Stage.class));
        verify(stageRepository).save(mockExistingStage);
        verify(auditClient).updateStageTeam(mockExistingStage);

        // -- Should be called
        verify(infoClient).getTeamForStageType(stageType);
        verify(auditClient).createStage(any(Stage.class));
        verify(auditClient, times(2)).updateStageTeam(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), any(UUID.class), eq(teamUUID), any(),
            eq(allocationType.toString()));
        verify(infoClient).getUserForTeam(teamUUID, userUUID);

        // -- Should NOT be called
        verify(auditClient, times(0)).updateStageUser(any(Stage.class));
        verify(notifyClient, times(0)).sendUserEmail(eq(caseData.getUuid()), any(UUID.class), any(UUID.class),
            eq(userUUID), anyString());

        verifyNoMoreInteractions(stageRepository, notifyClient, auditClient, infoClient, caseDataService,
            deadlineService, extensionService);
    }

    @Test
    public void testShouldCreateStageWithExtendedDeadline() {

        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(true); // -- test condition
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        // WHEN
        Stage createdStage = stageService.createStage(caseData.getUuid(), request);

        // THEN
        assertThat(createdStage.getDeadline()).isEqualTo(caseData.getCaseDeadline());
        assertThat(createdStage.getDeadlineWarning()).isEqualTo(caseData.getCaseDeadlineWarning());

        // -- Always called
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(infoClient).getAllStagesForCaseType(caseData.getType());
        verify(deadlineService).calculateWorkingDaysForStage(caseData.getType(), received, deadline,
            stageTypeDto.getSla());
        verify(extensionService).hasExtensions(caseData.getUuid());
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());
        verify(stageRepository, times(3)).save(any(Stage.class));
        verify(stageRepository).save(mockExistingStage);
        verify(auditClient).updateStageTeam(mockExistingStage);

        // -- Should be called
        verify(infoClient).getTeamForStageType(stageType);
        verify(auditClient).createStage(any(Stage.class));
        verify(auditClient, times(2)).updateStageTeam(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), any(UUID.class), eq(teamUUID), any(),
            eq(allocationType.toString()));

        // -- Should NOT be called
        verify(infoClient, times(0)).getUserForTeam(any(), any());
        verify(auditClient, times(0)).updateStageUser(any(Stage.class));
        verify(notifyClient, times(0)).sendUserEmail(eq(caseData.getUuid()), any(UUID.class), any(UUID.class),
            any(UUID.class), anyString());

        verifyNoMoreInteractions(stageRepository, notifyClient, auditClient, infoClient);
    }

    @Test
    public void testShouldCreateStageWithStageDeadlineOverride() {
        // GIVEN
        String overrideKey = String.format("%s_DEADLINE", stageType);
        String overrideDeadline = "2021-03-01";

        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        // -- test condition
        caseData.getDataMap().put(overrideKey, overrideDeadline);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(false);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        // WHEN
        Stage stage = stageService.createStage(caseData.getUuid(), request);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(LocalDate.parse(overrideDeadline));
        assertThat(stage.getDeadlineWarning()).isNull();
    }

    @Test
    public void testShouldCreateStageWithStageOverrideAndExtendedDeadlineWithOverrideDeadlineApplied() {
        // GIVEN
        String overrideKey = String.format("%s_DEADLINE", stageType);

        // -- test conditions Override after extended deadline.
        String overrideDeadline = "2021-03-01";
        LocalDate caseDeadline = LocalDate.of(2021, 2, 10);

        Map<String, String> caseDataData = new HashMap<>();
        caseDataData.put(overrideKey, overrideDeadline.toString());
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CaseData caseData = new CaseData(caseDataType, 12344567L, caseDeadline);
        caseData.update(caseDataData);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        // -- test condition
        caseData.getDataMap().put(overrideKey, overrideDeadline);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(true);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        // WHEN
        Stage stage = stageService.createStage(caseData.getUuid(), request);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(LocalDate.parse(overrideDeadline));
        assertThat(stage.getDeadlineWarning()).isNull();
    }

    @Test
    public void testShouldCreateStageWithStageOverrideAndExtendedDeadlineWithOverrideExtendedDeadlineApplied() {
        // GIVEN
        String overrideKey = String.format("%s_DEADLINE", stageType);

        // -- test conditions Override after extended deadline.
        String overrideDeadline = "2021-03-01";
        LocalDate caseDeadline = LocalDate.of(2021, 3, 10);
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        Map<String, String> caseDataData = new HashMap<>();
        caseDataData.put(overrideKey, overrideDeadline);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CaseData caseData = new CaseData(caseDataType, 12344567L, caseDeadline);
        caseData.update(caseDataData);
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        // -- test condition
        caseData.getDataMap().put(overrideKey, overrideDeadline);

        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);
        Stage mockExistingStage = new Stage(caseData.getUuid(), "ANOTHER_STAGE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        MOCK_STAGE_LIST.add(mockExistingStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);
        when(extensionService.hasExtensions(caseData.getUuid())).thenReturn(true);
        when(infoClient.getTeamForStageType(request.getType())).thenReturn(teamDto);

        // WHEN
        Stage stage = stageService.createStage(caseData.getUuid(), request);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(caseDeadline);
        assertThat(stage.getDeadlineWarning()).isEqualTo(caseDeadlineWarning);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void testShouldNotCreateStageMissingCaseUUIDException() {
        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);

        stageService.createStage(null, request);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void testShouldNotCreateStageMissingCaseUUID() {
        // GIVEN
        CreateStageRequest request = new CreateStageRequest(stageType, null, null, allocationType, transitionNoteUUID,
            null);

        // WHEN
        stageService.createStage(null, request);

        // THEN - exception test
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void testShouldNotCreateStageMissingTypeException() {
        // GIVEN
        CreateStageRequest request = new CreateStageRequest(null, null, teamUUID, allocationType, transitionNoteUUID,
            null);

        // WHEN
        stageService.createStage(caseUUID, request);

        // THEN - exception test
    }

    @Test
    public void testShouldRecreateStageWithoutTeamOrUserOverrideActiveStageNotCurrentStage() {

        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, stageUUID, null, null, null, null);
        Stage stageToRecreate = new Stage(caseData.getUuid(), stageType, null, null, null);
        stageToRecreate.setUuid(stageUUID);
        Stage currentActiveStage = new Stage(caseData.getUuid(), "ANOTHER_TYPE", UUID.randomUUID(), UUID.randomUUID(),
            transitionNoteUUID);
        currentActiveStage.setUuid(UUID.randomUUID());
        MOCK_STAGE_LIST.add(stageToRecreate);
        MOCK_STAGE_LIST.add(currentActiveStage);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(infoClient.getTeamForStageType(stageToRecreate.getStageType())).thenReturn(teamDto);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // THEN
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());

        verify(stageRepository, times(2)).save(stageToRecreate);
        verify(stageRepository).save(currentActiveStage);
        verify(auditClient).updateStageTeam(stageToRecreate);
        verify(auditClient).updateStageTeam(currentActiveStage);
        verify(auditClient).recreateStage(stageToRecreate);
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), eq(stageUUID), eq(teamUUID), any(),
            eq(allocationType));

        verify(auditClient, times(0)).updateStageUser(stageToRecreate);
        verifyNoMoreInteractions(auditClient, stageRepository, notifyClient);

    }

    @Test
    public void testShouldRecreateStageWithoutTeamOrUserOverrideActiveStageIsCurrentStage() {
        // GIVEN
        LocalDate received = LocalDate.parse("2021-01-04");
        LocalDate deadline = LocalDate.parse("2021-02-01");
        LocalDate deadlineWarning = LocalDate.parse("2021-01-25");
        CaseData caseData = new CaseData(caseDataType, 12344567L, received);
        caseData.setCaseDeadline(deadline);
        caseData.setCaseDeadlineWarning(deadlineWarning);

        TeamDto teamDto = new TeamDto(null, teamUUID, true, null);

        CreateStageRequest request = new CreateStageRequest(stageType, stageUUID, null, null, null, null);
        Stage stageToRecreate = new Stage(caseData.getUuid(), stageType, teamDto.getUuid(), userUUID, null);
        stageToRecreate.setUuid(stageUUID);
        MOCK_STAGE_LIST.add(stageToRecreate);

        when(stageRepository.findAllByCaseUUIDAsStage(caseData.getUuid())).thenReturn(MOCK_STAGE_LIST);
        when(infoClient.getTeamForStageType(stageToRecreate.getStageType())).thenReturn(teamDto);
        when(caseDataService.getCaseData(caseData.getUuid())).thenReturn(caseData);

        // WHEN
        stageService.createStage(caseData.getUuid(), request);

        // THEN
        verify(stageRepository).findAllByCaseUUIDAsStage(caseData.getUuid());
        verify(caseDataService).getCaseData(caseData.getUuid());
        verify(caseDataService).updateCaseData(eq(caseData), any(UUID.class), anyMap());

        verify(stageRepository, times(2)).save(stageToRecreate);
        verify(auditClient).updateStageTeam(stageToRecreate);
        verify(notifyClient).sendTeamEmail(eq(caseData.getUuid()), eq(stageUUID), eq(teamUUID), any(),
            eq(allocationType));

        verify(auditClient, times(1)).updateStageTeam(any(Stage.class));

        verify(auditClient, times(0)).recreateStage(stageToRecreate);
        verify(auditClient, times(0)).updateStageUser(stageToRecreate);

        verifyNoMoreInteractions(auditClient, stageRepository, notifyClient);
    }

    @Test
    public void shouldGetStageByCaseReferenceWithValidParams() {
        String ref = "MIN/0123456/19";

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseReference(ref)).thenReturn(Collections.singleton(stage));

        stageService.getActiveStagesByCaseReference(ref);

        verify(stageRepository).findByCaseReference(ref);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageByCaseReferenceWithMissingReference() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseReference(null)).thenReturn(Collections.singleton(stage));

        stageService.getActiveStagesByCaseReference(null);

        verify(stageRepository).findByCaseReference(null);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageWithValidParams() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.getActiveStage(caseUUID, stageUUID);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetStageWithValidParamsNotFoundException() {

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(null);

        stageService.getActiveStage(caseUUID, stageUUID);
    }

    @Test
    public void shouldNotGetStageWithValidParamsNotFound() {

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(null);

        try {
            stageService.getActiveStage(caseUUID, stageUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetStageMissingCaseUUIDException() {

        stageService.getActiveStage(null, stageUUID);
    }

    @Test()
    public void shouldNotGetStageMissingCaseUUID() {

        try {
            stageService.getActiveStage(null, stageUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository).findActiveByCaseUuidStageUUID(null, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetStageMissingStageUUIDException() {

        stageService.getActiveStage(caseUUID, null);
    }

    @Test()
    public void shouldNotGetStageMissingStageUUID() {

        try {
            stageService.getActiveStage(caseUUID, null);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, null);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test
    public void shouldGetActiveStagesCaseUUID() {
        stageService.getActiveStagesByCaseUUID(caseUUID);

        verify(stageRepository).findAllActiveByCaseUUID(caseUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);
    }

    @Test
    public void shouldGetActiveStages_blankResult() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUsersTeams();

        verify(stageRepository).findAllActiveByTeamUUID(teams);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);
    }

    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(stageRepository.findAllActiveByTeamUUID(teams)).thenReturn(Set.of(stage));

        stageService.getActiveStagesForUsersTeams();

        verify(userPermissionsService).getExpandedUserTeams();
        verify(stageRepository).findAllActiveByTeamUUID(teams);
        verify(stagePriorityCalculator).updatePriority(stage, stage.getCaseDataType());
        verify(daysElapsedCalculator).updateDaysElapsed(stage.getData(), stage.getCaseDataType());

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveStagesEmpty() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUsersTeams();

        // We don't try and get active stages with no teams (empty set) because we're going to get 0 results.
        verify(userPermissionsService).getExpandedUserTeams();
        checkNoMoreInteraction();

    }

    @Test
    public void shouldUpdateStageTransitionNote() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveBasicStageByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageCurrentTransitionNote(caseUUID, stageUUID, transitionNoteUUID);

        verify(stageRepository).findActiveBasicStageByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        checkNoMoreInteraction();

    }

    @Test
    public void shouldUpdateStageTransitionNoteNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveBasicStageByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageCurrentTransitionNote(caseUUID, stageUUID, null);

        verify(stageRepository).findActiveBasicStageByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        checkNoMoreInteraction();

    }

    @Test
    public void testShouldUpdateStageTeam() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, teamUUID, allocationType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);
        verify(auditClient).updateStageTeam(stage);
        verify(notifyClient).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null),
            eq(allocationType.toString()));

        checkNoMoreInteraction();

    }

    @Test
    public void testShouldAuditUpdateStageTeam() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, teamUUID, "ALLOCATE_TEAM");

        verify(auditClient).updateStageTeam(stage);
        verify(stageRepository).findByCaseUuidStageUUID(eq(caseUUID), any());
        verify(stageRepository).save(stage);
        verify(notifyClient).sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, null, "ALLOCATE_TEAM");

        checkNoMoreInteraction();
    }

    @Test
    public void shouldUpdateStageTeamNull() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, null, allocationType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);

    }

    @Test
    public void shouldUpdateStageUser() {

        UUID newUserUUID = UUID.randomUUID();
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, newUserUUID);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);
        verify(notifyClient).sendUserEmail(eq(caseUUID), any(UUID.class), eq(userUUID), eq(newUserUUID), eq(null));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);

    }

    @Test
    public void shouldAuditUpdateStageUser() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(auditClient).updateStageUser(stage);
        verifyNoMoreInteractions(auditClient);

    }

    @Test
    public void shouldUpdateStageUserNull() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, null, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, null);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);
        verify(notifyClient).sendUserEmail(eq(caseUUID), any(UUID.class), eq(null), eq(null), eq(null));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);

    }

    @Test
    public void shouldGetActiveStageCaseUUIDsForUserAndTeam() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Set<Stage> stages = new HashSet<>();
        stages.add(stage);

        when(stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(userUUID, teamUUID)).thenReturn(stages);

        stageService.getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);

        verify(stageRepository).findStageCaseUUIDsByUserUUIDTeamUUID(userUUID, teamUUID);
        verifyNoMoreInteractions(stageRepository);

    }

    @Test
    public void shouldSearch() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = stageService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageRepository).findAllByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageRepository);

        assertThat(stageResults).hasSize(1);

    }

    @Test
    public void shouldSearchCaseAndNextCaseTypesPresent() {
        StageWithCaseData stageFound = testCaseWithNextCaseType(Boolean.TRUE);
        assertThat(stageFound.getNextCaseType()).isNotBlank();
    }

    @Test
    public void shouldSearchIncompleteCaseAndNextCaseTypesPresent() {
        StageWithCaseData stageFound = testCaseWithNextCaseType(Boolean.FALSE);
        assertThat(stageFound.getNextCaseType()).isNull();
    }

    private StageWithCaseData testCaseWithNextCaseType(Boolean completeCase) {

        // given
        Set<UUID> caseUUIDS = Set.of(caseUUID);
        StageWithCaseData repositoryStage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        repositoryStage.setCompleted(completeCase);
        repositoryStage.setCaseDataType("MIN");

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(Set.of(repositoryStage));

        when(infoClient.getAllCaseTypes()).thenReturn(caseDataTypes);

        // when
        Set<StageWithCaseData> stageResults = stageService.search(searchRequest);

        // then
        verify(searchClient).search(searchRequest);
        verify(stageRepository).findAllByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageRepository);

        assertThat(stageResults).hasSize(1);

        return stageResults.iterator().next();

    }

    @Test
    public void shouldSearchInactiveStage() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        StageWithCaseData stage_old = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = stageService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageRepository).findAllByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageRepository);

        assertThat(stageResults).hasSize(1);

    }

    @Test
    public void shouldSearchMultipleStages() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        StageWithCaseData stage_old = new StageWithCaseData(UUID.randomUUID(), "DCU_MIN_MARKUP", null, null,
            transitionNoteUUID);
        Set<StageWithCaseData> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<StageWithCaseData> stageResults = stageService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageRepository).findAllByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageRepository);

        assertThat(stageResults).hasSize(2);

    }

    @Test
    public void shouldSearchNoResults() {

        Set<UUID> caseUUIDS = new HashSet<>(0);

        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);

        stageService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verifyNoMoreInteractions(searchClient);
        verifyNoInteractions(stageRepository);

    }

    @Test
    public void shouldCheckSendOfflineQAEmail() {

        // GIVEN
        UUID offlineQaUserUUID = UUID.randomUUID();
        StageWithCaseData stage = new StageWithCaseData(caseUUID, StageWithCaseData.DCU_DTEN_INITIAL_DRAFT, teamUUID,
            userUUID, transitionNoteUUID);
        stage.putData(StageWithCaseData.OFFLINE_QA_USER, offlineQaUserUUID.toString());

        List<String> auditType = new ArrayList<>();
        auditType.add(STAGE_ALLOCATED_TO_USER.name());
        final Set<GetAuditResponse> auditLines = getAuditLines(stage);

        when(auditClient.getAuditLinesForCase(caseUUID, auditType)).thenReturn(auditLines);

        // WHEN
        stageService.checkSendOfflineQAEmail(stage);

        // THEN
        verify(auditClient).getAuditLinesForCase(caseUUID, auditType);
        verify(notifyClient).sendOfflineQaEmail(stage.getCaseUUID(), stage.getUuid(), userUUID, offlineQaUserUUID,
            stage.getCaseReference());
    }

    @Test
    public void getAllStagesForCaseByUUID() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        Set<StageWithCaseData> stages = Set.of(stage);

        when(stageRepository.findAllByCaseUUID(caseUUID)).thenReturn(stages);

        Set<StageWithCaseData> result = stageService.getAllStagesForCaseByCaseUUID(caseUUID);
        Assert.assertEquals(stages, result);
    }

    @Test
    public void shouldGetStageTypeFromStageData() {
        String stageType = "a-stage-type";
        Stage stage = new Stage(UUID.randomUUID(), stageType, teamUUID, null, null);

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(caseUUID, stageUUID)).thenReturn(stage);

        String result = stageService.getStageType(caseUUID, stageUUID);

        Assert.assertEquals(stageType, result);
    }

    @Test
    public void shouldGetStageTypeFromStageData_Inactive() {
        String stageType = "a-stage-type";
        Stage stage = new Stage(UUID.randomUUID(), stageType, null, null, null);

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(caseUUID, stageUUID)).thenReturn(stage);

        String result = stageService.getStageType(caseUUID, stageUUID);

        Assert.assertEquals(stageType, result);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldGetStageTypeFromStageData_Null() {

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(caseUUID, stageUUID)).thenReturn(null);

        stageService.getStageType(caseUUID, stageUUID);

        verify(stageRepository).findAllByCaseUUID(caseUUID);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getAllStagesForCaseByUuidThrowsExceptionIfEmpty() {
        Set<StageWithCaseData> stages = Set.of();

        when(stageRepository.findAllByCaseUUID(caseUUID)).thenReturn(stages);

        stageService.getAllStagesForCaseByCaseUUID(caseUUID);
        verify(stageRepository).findAllByCaseUUID(caseUUID);
    }

    @Test
    public void withdrawCase() {

        WithdrawCaseRequest withdrawCaseRequest = new WithdrawCaseRequest("Note 1", "2010-11-23");
        CaseData mockedCaseData = mock(CaseData.class);
        ActiveStage activeStage1 = new ActiveStage(1L, UUID.randomUUID(), LocalDateTime.now(), "MPAM", LocalDate.now(),
            LocalDate.now(), UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        ActiveStage activeStage2 = new ActiveStage(2L, UUID.randomUUID(), LocalDateTime.now(), "MPAM", LocalDate.now(),
            LocalDate.now(), UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        StageWithCaseData stage1 = new StageWithCaseData(caseUUID, "stageType1", teamUUID, userUUID,
            transitionNoteUUID);
        StageWithCaseData stage2 = new StageWithCaseData(caseUUID, "stageType2", teamUUID, userUUID,
            transitionNoteUUID);

        when(mockedCaseData.getActiveStages()).thenReturn(Sets.newLinkedHashSet(activeStage1, activeStage2));
        when(stageRepository.findByCaseUuidStageUUID(caseUUID, activeStage1.getUuid())).thenReturn(stage1);
        when(stageRepository.findByCaseUuidStageUUID(caseUUID, activeStage2.getUuid())).thenReturn(stage2);
        when(caseDataService.getCaseData(caseUUID)).thenReturn(mockedCaseData);

        stageService.withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);

        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("Withdrawn", "True");
        expectedData.put("WithdrawalDate", "2010-11-23");
        expectedData.put("WithdrawalReason", "Note 1");
        expectedData.put("CurrentStage", "");

        verify(caseDataService).getCaseData(caseUUID);
        verify(caseDataService).updateCaseData(caseUUID, stageUUID, expectedData);
        verify(caseDataService).completeCase(caseUUID, true);
        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, activeStage1.getUuid());
        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, activeStage2.getUuid());
        verify(stageRepository).save(stage1);
        verify(stageRepository).save(stage2);
        verify(auditClient).updateStageTeam(stage1);
        verify(auditClient).updateStageTeam(stage2);
        verify(caseNoteService).createCaseNote(caseUUID, "WITHDRAW", "Note 1");
        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetUnassignedAndActiveStageByTeamUUID() {
        stageService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID);

        verify(stageRepository).findAllUnassignedAndActiveByTeamUUID(teamUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyNoInteractions(notifyClient);
    }

    @Test
    public void shouldGetActiveStagesByTeamUuids() {
        Set<UUID> teamUuids = Set.of(teamUUID);
        StageWithCaseData stage1 = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        StageWithCaseData stage2 = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        Set<StageWithCaseData> stages = Set.of(stage1, stage2);

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teamUuids);
        when(stageRepository.findAllActiveByTeamUUID(teamUUID)).thenReturn(stages);

        stageService.getActiveStagesByTeamUUID(teamUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(contributionsProcessor).processContributionsForStages(stages);
        verify(stageRepository).findAllActiveByTeamUUID(teamUUID);
    }

    @Test(expected = SecurityExceptions.ForbiddenException.class)
    public void shouldGetActiveStagesByTeamUuids_ForbiddenThrownWhenNotInTeam() {
        Set<UUID> teamUuids = Set.of(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teamUuids);

        stageService.getActiveStagesByTeamUUID(teamUUID);
    }

    @Test
    public void shouldGetActiveUserStagesWithTeams() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(stageRepository.findAllActiveByUserUuidAndTeamUuid(userUUID, teams)).thenReturn(Set.of(stage));

        stageService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(stageRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);
        verify(stagePriorityCalculator).updatePriority(stage, stage.getCaseDataType());
        verify(daysElapsedCalculator).updateDaysElapsed(stage.getData(), stage.getCaseDataType());

        checkNoMoreInteraction();
    }

    @Test
    public void shouldSetTagsOnStages() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID,
            transitionNoteUUID);
        stage.putData("HomeSecReply", "TRUE");

        ArrayList<String> tags = new ArrayList<String>(Collections.singleton("HS"));

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);
        when(stageRepository.findAllActiveByUserUuidAndTeamUuid(userUUID, teams)).thenReturn(Set.of(stage));
        when(stageTagsDecorator.decorateTags(stage.getData(), stage.getStageType())).thenReturn(tags);

        var result = stageService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        verify(stageRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);
        verify(stagePriorityCalculator).updatePriority(stage, stage.getCaseDataType());
        verify(daysElapsedCalculator).updateDaysElapsed(stage.getData(), stage.getCaseDataType());
        verify(stageTagsDecorator).decorateTags(stage.getData(), stage.getStageType());

        assertThat(result.iterator().next().getTag()).isEqualTo(tags);

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveUserStagesWithTeamsAndCaseType_noTeams() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        stageService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(userPermissionsService).getExpandedUserTeams();
        checkNoMoreInteraction();
    }

    @Test
    public void getActiveUserStagesWithTeamsAndCaseType_blankResult() {
        Set<UUID> teams = Set.of(UUID.randomUUID());

        when(userPermissionsService.getExpandedUserTeams()).thenReturn(teams);

        stageService.getActiveUserStagesWithTeamsForUser(userUUID);

        verify(stageRepository).findAllActiveByUserUuidAndTeamUuid(userUUID, teams);

        verifyNoMoreInteractions(stageRepository, notifyClient);
    }

    @Test
    public void getStageTeam_valid() {
        var caseUuid = UUID.randomUUID();
        var stageUuid = UUID.randomUUID();
        var teamUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUuid, "A Type", teamUUID, null, null);

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(any(), any())).thenReturn(stage);

        var result = stageService.getStageTeam(caseUuid, stageUuid);
        assertThat(result).isNotNull();
        assertThat(result.toString()).hasToString(teamUUID.toString());

        verify(stageRepository).findBasicStageByCaseUuidAndStageUuid(caseUuid, stageUuid);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getStageTeam_nullResult() {
        var caseUuid = UUID.randomUUID();
        var stageUuid = UUID.randomUUID();

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(any(), any())).thenReturn(null);

        stageService.getStageTeam(caseUuid, stageUuid);
    }

    /**
     * The stage cannot be an instance as it does not have a function to set data (in the Stage Class).
     * I did not want to create a setData on the Stage class for testing only.
     *
     * @return Mocked Stage for setting and exposing the DATA with offline QA user.
     */
    private StageWithCaseData createStageOfflineQaData(UUID offlineQaUserUUID) {
        StageWithCaseData mockStage = mock(StageWithCaseData.class);
        when(mockStage.getUuid()).thenReturn(stageUUID);
        when(mockStage.getCaseUUID()).thenReturn(caseUUID);
        when(mockStage.getStageType()).thenReturn(StageWithCaseData.DCU_DTEN_INITIAL_DRAFT);
        when(mockStage.getCaseReference()).thenReturn("MIN/1234567/19");
        when(mockStage.getData(StageWithCaseData.OFFLINE_QA_USER)).thenReturn(offlineQaUserUUID.toString());
        return mockStage;
    }

    private Set<GetAuditResponse> getAuditLines(StageWithCaseData stage) {
        Set<GetAuditResponse> linesForCase = new HashSet<>();
        linesForCase.add(
            new GetAuditResponse(UUID.randomUUID(), caseUUID, stage.getUuid(), UUID.randomUUID().toString(), "", "{}",
                "", ZonedDateTime.now(), STAGE_ALLOCATED_TO_USER.name(), stage.getUserUUID().toString()));
        return linesForCase;
    }

    private void checkNoMoreInteraction() {
        verifyNoMoreInteractions(stageRepository, userPermissionsService, notifyClient, auditClient, searchClient,
            infoClient, caseDataService, stagePriorityCalculator, daysElapsedCalculator, caseNoteService);
    }

}
