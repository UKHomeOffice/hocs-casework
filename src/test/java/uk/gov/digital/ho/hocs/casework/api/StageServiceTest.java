package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.json.Jackson;
import org.assertj.core.util.Sets;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.WithdrawCaseRequest;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.client.searchclient.SearchClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.priority.StagePriorityCalculator;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;

@RunWith(MockitoJUnitRunner.class)
public class  StageServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final String stageType = "DCU_MIN_MARKUP";
    private final String allocationType = "anyAllocate";
    private final UUID transitionNoteUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN");
    private final String userID = UUID.randomUUID().toString();

    private StageService stageService;

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
    private CaseNoteService caseNoteService;
    @Mock
    private Stage stage;

    @Before
    public void setUp() {
        this.stageService = new StageService(stageRepository, userPermissionsService, notifyClient, auditClient,
                searchClient, infoClient, caseDataService, stagePriorityCalculator, daysElapsedCalculator, caseNoteService);
    }

    @Test
    public void shouldCreateStage() {

        CaseData caseData = new CaseData(caseDataType, 12344567L, LocalDate.now());
        caseData.setCaseDeadlineWarning(LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        verify(caseDataService).getCase(caseUUID);
        verify(infoClient).getStageDeadline(stageType, caseData.getDateReceived(), caseData.getCaseDeadline());
        verify(infoClient).getStageDeadlineWarning(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning());
        verify(stageRepository).save(any(Stage.class));
        verify(notifyClient).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);

    }

    @Test
    public void shouldAuditCreateStage() {

        CaseData caseData = new CaseData(caseDataType, 12344567L, LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        verify(auditClient).createStage(any(Stage.class));
        verifyNoMoreInteractions(auditClient);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateStageMissingCaseUUIDException() {

        stageService.createStage(null, stageType, teamUUID, userUUID, null, transitionNoteUUID);
    }

    @Test()
    public void shouldNotCreateStageMissingCaseUUID() {

        try {
            stageService.createStage(null, stageType, teamUUID, userUUID, null, transitionNoteUUID);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {

        stageService.createStage(caseUUID, null, teamUUID, userUUID, null, transitionNoteUUID);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        try {
            stageService.createStage(caseUUID, null, teamUUID, userUUID, null, transitionNoteUUID);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldRecreateStage() {
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.recreateStage(caseUUID, stageUUID, stageType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(auditClient).recreateStage(stage);

        verifyNoMoreInteractions(auditClient, stageRepository, notifyClient);

    }

    @Test
    public void shouldGetStageByCaseReferenceWithValidParams() {
        String ref = "MIN/0123456/19";

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseReference(ref)).thenReturn(Collections.singleton(stage));

        stageService.getActiveStagesByCaseReference(ref);

        verify(stageRepository).findByCaseReference(ref);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageByCaseReferenceWithMissingReference() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseReference(null)).thenReturn(Collections.singleton(stage));

        stageService.getActiveStagesByCaseReference(null);

        verify(stageRepository).findByCaseReference(null);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageWithValidParams() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.getActiveStage(caseUUID, stageUUID);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

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
        verifyZeroInteractions(notifyClient);

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
        verifyZeroInteractions(notifyClient);

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
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetActiveStagesCaseUUID() {

        stageService.getActiveStagesByCaseUUID(caseUUID);

        verify(stageRepository).findAllActiveByCaseUUID(caseUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetActiveStageCaseUUID() {
        stageService.getActiveStageByCaseUUID(caseUUID);

        verify(stageRepository).findAllActiveByCaseUUID(caseUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetActiveStages_blankResult() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());
        Set<String> caseTypes = Set.of("");

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUser();

        verify(stageRepository).findAllActiveByTeamUUIDAndCaseType(teams, caseTypes);

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
    }


    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();
        Set<String> caseTypes = Set.of("CASE_TYPE1", "CASE_TYPE2");
        teams.add(UUID.randomUUID());
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(userPermissionsService.getUserTeams()).thenReturn(teams);
        when(userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(caseTypes);
        when(stageRepository.findAllActiveByTeamUUIDAndCaseType(teams, caseTypes)).thenReturn(Set.of(stage));

        stageService.getActiveStagesForUser();

        verify(userPermissionsService).getUserTeams();
        verify(userPermissionsService).getCaseTypesIfUserTeamIsCaseTypeAdmin();
        verify(stageRepository).findAllActiveByTeamUUIDAndCaseType(teams, caseTypes);
        verify(stagePriorityCalculator).updatePriority(stage);
        verify(daysElapsedCalculator).updateDaysElapsed(stage);

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveStagesEmpty() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUser();

        // We don't try and get active stages with no teams (empty set) because we're going to get 0 results.
        verify(userPermissionsService).getUserTeams();
        checkNoMoreInteraction();

    }

    @Test
    public void shouldUpdateStageTransitionNote() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageCurrentTransitionNote(caseUUID, stageUUID, transitionNoteUUID);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        checkNoMoreInteraction();

    }

    @Test
    public void shouldUpdateStageTransitionNoteNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageCurrentTransitionNote(caseUUID, stageUUID, null);

        verify(stageRepository).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        checkNoMoreInteraction();

    }

    @Test
    public void shouldUpdateStageTeam() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, teamUUID, allocationType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);
        verify(auditClient).updateStageTeam(stage);
        verify(caseDataService).getCaseRef(caseUUID);
        verify(notifyClient).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));

        checkNoMoreInteraction();

    }


    @Test
    public void shouldAuditUpdateStageTeam() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, teamUUID, null);

        verify(auditClient).updateStageTeam(stage);
        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);
        verify(notifyClient).sendTeamEmail(caseUUID, stage.getUuid(), teamUUID, null, null);
        verify(caseDataService).getCaseRef(caseUUID);

        checkNoMoreInteraction();

    }


    @Test
    public void shouldUpdateStageTeamNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageTeam(caseUUID, stageUUID, null, allocationType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository).save(stage);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldUpdateStageUser() {

        UUID newUserUUID = UUID.randomUUID();
        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(auditClient).updateStageUser(stage);
        verifyNoMoreInteractions(auditClient);

    }

    @Test
    public void shouldUpdateStageUserNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, null, transitionNoteUUID);

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

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Set<Stage> stages = new HashSet<>();
        stages.add(stage);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<Stage> stageResults = stageService.search(searchRequest);

        verify(searchClient).search(searchRequest);
        verify(stageRepository).findAllByCaseUUIDIn(caseUUIDS);
        verifyNoMoreInteractions(searchClient);
        verifyNoMoreInteractions(stageRepository);

        assertThat(stageResults).hasSize(1);

    }

    @Test
    public void shouldSearchInactiveStage() {

        Set<UUID> caseUUIDS = new HashSet<>();
        caseUUIDS.add(caseUUID);

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Stage stage_old = new Stage(caseUUID, "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
        Set<Stage> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<Stage> stageResults = stageService.search(searchRequest);

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

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Stage stage_old = new Stage(UUID.randomUUID(), "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
        Set<Stage> stages = new HashSet<>();
        stages.add(stage);
        stages.add(stage_old);


        SearchRequest searchRequest = new SearchRequest();

        when(searchClient.search(searchRequest)).thenReturn(caseUUIDS);
        when(stageRepository.findAllByCaseUUIDIn(caseUUIDS)).thenReturn(stages);

        Set<Stage> stageResults = stageService.search(searchRequest);

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
        verifyZeroInteractions(stageRepository);

    }

    @Test
    public void shouldGetOfflineQaUser() {
        UUID offlineQaUserUUID = UUID.randomUUID();
        Map dataMap = new HashMap();
        dataMap.put(Stage.OFFLINE_QA_USER, offlineQaUserUUID.toString());
        final String offlineQaUser = stageService.getOfflineQaUser(Jackson.toJsonString(dataMap));
        assertThat(offlineQaUser).isEqualTo(offlineQaUserUUID.toString());
    }

    @Test
    public void shouldCheckSendOfflineQAEmail() {
        UUID offlineQaUserUUID = UUID.randomUUID();
        Stage stage = createStageOfflineQaData(offlineQaUserUUID);
        List<String> auditType = new ArrayList<>();
        auditType.add(STAGE_ALLOCATED_TO_USER.name());
        final Set<GetAuditResponse> auditLines = getAuditLines(stage);
        when(auditClient.getAuditLinesForCase(caseUUID, auditType)).thenReturn(auditLines);
        when(caseDataService.getCaseRef(caseUUID)).thenReturn("MIN/1234567/19");
        stageService.checkSendOfflineQAEmail(stage);
        verify(auditClient).getAuditLinesForCase(caseUUID, auditType);
        verify(notifyClient).sendOfflineQaEmail(stage.getCaseUUID(), stage.getUuid(), UUID.fromString(userID), offlineQaUserUUID, stage.getCaseReference());
    }

    @Test
    public void getAllStagesForCaseByUUID() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        Set<Stage> stages = Set.of(stage);

        when(stageRepository.findAllByCaseUUID(caseUUID)).thenReturn(stages);

        Set<Stage> result = stageService.getAllStagesForCaseByCaseUUID(caseUUID);
        Assert.assertEquals(stages, result);
    }

    @Test
    public void shouldGetStageTypeFromStageData() {
        String stageType = "a-stage-type";

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);
        when(stage.getStageType()).thenReturn(stageType);

        String result = stageService.getStageTypeFromStageData(caseUUID, stageUUID);
        Assert.assertEquals(stageType, result);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void getAllStagesForCaseByUuidThrowsExceptionIfEmpty() {
        Set<Stage> stages = Set.of();

        when(stageRepository.findAllByCaseUUID(caseUUID)).thenReturn(stages);

        stageService.getAllStagesForCaseByCaseUUID(caseUUID);
        verify(stageRepository).findAllByCaseUUID(caseUUID);
    }

    @Test
    public void withdrawCase(){

        WithdrawCaseRequest withdrawCaseRequest = new WithdrawCaseRequest("Note 1", "2010-11-23");
        CaseData mockedCaseData = mock(CaseData.class);
        ActiveStage activeStage1 = new ActiveStage(1L,
                UUID.randomUUID(), LocalDateTime.now(),"MPAM", LocalDate.now(), LocalDate.now(),
                UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        ActiveStage activeStage2 = new ActiveStage(2L,
                UUID.randomUUID(), LocalDateTime.now(),"MPAM", LocalDate.now(), LocalDate.now(),
                UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        Stage stage1 = new Stage(caseUUID, "stageType1", teamUUID, userUUID, transitionNoteUUID);
        Stage stage2 = new Stage(caseUUID, "stageType2", teamUUID, userUUID, transitionNoteUUID);

        when(mockedCaseData.getActiveStages()).thenReturn(Sets.newLinkedHashSet(activeStage1, activeStage2));
        when(stageRepository.findByCaseUuidStageUUID(caseUUID, activeStage1.getUuid())).thenReturn(stage1);
        when(stageRepository.findByCaseUuidStageUUID(caseUUID, activeStage2.getUuid())).thenReturn(stage2);
        when(caseDataService.getCase(caseUUID)).thenReturn(mockedCaseData);

        stageService.withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);

        Map<String, String> expectedData = new HashMap<>();
        expectedData.put("Withdrawn", "True");
        expectedData.put("WithdrawalDate", "2010-11-23");
        expectedData.put("CurrentStage", "");

        verify(caseDataService).getCase(caseUUID);
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
        verifyZeroInteractions(notifyClient);
    }

    /**
     * The stage cannot be an instance as it does not have a function to set data (in the Stage Class).
     * I did not want to create a setData on the Stage class for testing only.
     *
     * @return Mocked Stage for setting and exposing the DATA with offline QA user.
     */
    private Stage createStageOfflineQaData(UUID offlineQaUserUUID) {
        Map dataMap = new HashMap();
        dataMap.put(Stage.OFFLINE_QA_USER, offlineQaUserUUID.toString());
        Stage mockStage = mock(Stage.class);
        when(mockStage.getUuid()).thenReturn(stageUUID);
        when(mockStage.getCaseUUID()).thenReturn(caseUUID);
        when(mockStage.getStageType()).thenReturn(Stage.DCU_DTEN_INITIAL_DRAFT);
        when(mockStage.getCaseReference()).thenReturn("MIN/1234567/19");
        when(mockStage.getData()).thenReturn(Jackson.toJsonString(dataMap));
        return mockStage;
    }

    private Set<GetAuditResponse> getAuditLines(Stage stage) {
        Set<GetAuditResponse> linesForCase = new HashSet<>();
        linesForCase.add(new GetAuditResponse(UUID.randomUUID(), caseUUID, stage.getUuid(), UUID.randomUUID().toString(), "",
                "{}", "", ZonedDateTime.now(), STAGE_ALLOCATED_TO_USER.name(), userID));
        return linesForCase;
    }

    private void checkNoMoreInteraction() {
        verifyNoMoreInteractions(stageRepository, userPermissionsService, notifyClient, auditClient, searchClient,
                infoClient, caseDataService, stagePriorityCalculator, daysElapsedCalculator, caseNoteService);
    }

}
