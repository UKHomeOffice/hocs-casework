package uk.gov.digital.ho.hocs.casework.api;

import com.amazonaws.util.json.Jackson;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.auditclient.dto.GetAuditResponse;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
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
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.client.auditclient.EventType.STAGE_ALLOCATED_TO_USER;

@RunWith(MockitoJUnitRunner.class)
public class StageServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final String stageType = "DCU_MIN_MARKUP";
    private final String allocationType = "anyAllocate";
    private final UUID transitionNoteUUID = UUID.randomUUID();
    private final CaseDataType caseDataType = new CaseDataType("MIN", "1a", "MIN", null);
    private final List<CaseDataType> caseDataTypes = List.of(
            CaseDataTypeFactory.from("NXT", "a5", "MIN"), // NXT can be reached through MIN
                caseDataType);

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
    private StageTagsDecorator stageTagsDecorator;
    @Mock
    private CaseNoteService caseNoteService;
    @Mock
    private ContributionsProcessor contributionsProcessor;
    @Mock
    private StageWithCaseData stage;

    @Mock
    private ActionDataDeadlineExtensionService extensionService;

    @Before
    public void setUp() {
        this.stageService = new StageService(stageRepository, userPermissionsService, notifyClient, auditClient,
                searchClient, infoClient, caseDataService, stagePriorityCalculator, daysElapsedCalculator, stageTagsDecorator, caseNoteService, contributionsProcessor, extensionService);
    }

    @Test
    public void shouldCreateStage() {

        CaseData caseData = new CaseData(caseDataType, 12344567L, LocalDate.now());
        caseData.setCaseDeadlineWarning(LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);
        when(extensionService.hasExtensions(caseUUID)).thenReturn(false);

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        verify(caseDataService).getCase(caseUUID);
        verify(infoClient).getStageDeadline(stageType, caseData.getDateReceived(), caseData.getCaseDeadline());
        verify(infoClient).getStageDeadlineWarning(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning());
        verify(stageRepository).save(any(StageWithCaseData.class));
        verify(notifyClient).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);

    }

    @Test
    public void shouldCreateStage_ExtendedDeadline() {

        // GIVEN
        Map<String, String> caseDataData = new HashMap<>();
        LocalDate caseDeadline = LocalDate.now();
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        CaseData caseData = new CaseData(caseDataType, 12344567L,caseDeadline);
        caseData.update(caseDataData, new ObjectMapper());
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);
        when(extensionService.hasExtensions(caseUUID)).thenReturn(true);
        when(infoClient.getStageDeadlineOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadline())).thenReturn(caseData.getCaseDeadline());
        when(infoClient.getStageDeadlineWarningOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning())).thenReturn(caseData.getCaseDeadlineWarning());

        // WHEN
        StageWithCaseData stage = stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(caseDeadline);
        assertThat(stage.getDeadlineWarning()).isEqualTo(caseDeadlineWarning);

    }

    @Test
    public void shouldCreateStage_stageOverride() throws JsonProcessingException {
        // GIVEN
        LocalDate overrideDeadline = LocalDate.of(2021, 12, 31);
        Map<String, String> caseDataData = new HashMap<>();
        String overrideKey = String.format("%s_DEADLINE", stageType);
        caseDataData.put(overrideKey, overrideDeadline.toString());
        LocalDate caseDeadline = LocalDate.now();
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        CaseData caseData = new CaseData(caseDataType, 12344567L,caseDeadline);
        caseData.update(caseDataData, new ObjectMapper());
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        when(caseDataService.getCaseDataField(caseUUID,overrideKey)).thenReturn(overrideDeadline.toString());
        when(extensionService.hasExtensions(caseUUID)).thenReturn(false);

        // WHEN
        StageWithCaseData stage = stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(overrideDeadline);
        assertThat(stage.getDeadlineWarning()).isNull();
    }

    @Test
    public void shouldCreateStage_stageOverrideAndLaterExtendedDeadline() {
        // GIVEN
        LocalDate overrideDeadline = LocalDate.of(2021, 12, 1);
        Map<String, String> caseDataData = new HashMap<>();
        String overrideKey = String.format("%s_DEADLINE", stageType);
        caseDataData.put(overrideKey, overrideDeadline.toString());
        LocalDate caseDeadline = LocalDate.of(2021,12,10);
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        CaseData caseData = new CaseData(caseDataType, 12344567L,caseDeadline);
        caseData.update(caseDataData, new ObjectMapper());
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        when(caseDataService.getCaseDataField(caseUUID,overrideKey)).thenReturn(overrideDeadline.toString());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);
        when(extensionService.hasExtensions(caseUUID)).thenReturn(true);
        when(infoClient.getStageDeadlineOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadline())).thenReturn(caseData.getCaseDeadline());
        when(infoClient.getStageDeadlineWarningOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning())).thenReturn(caseData.getCaseDeadlineWarning());

        // WHEN
        StageWithCaseData stage = stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(caseDeadline);
        assertThat(stage.getDeadlineWarning()).isEqualTo(caseDeadlineWarning);
    }

    @Test
    public void shouldCreateStage_stageExtendedDeadlineAndLaterOverride() {
        // GIVEN
        LocalDate overrideDeadline = LocalDate.of(2021, 12, 10);
        Map<String, String> caseDataData = new HashMap<>();
        String overrideKey = String.format("%s_DEADLINE", stageType);
        caseDataData.put(overrideKey, overrideDeadline.toString());
        LocalDate caseDeadline = LocalDate.of(2021,12,1);
        LocalDate caseDeadlineWarning = caseDeadline.minusDays(2);

        CaseData caseData = new CaseData(caseDataType, 12344567L,caseDeadline);
        caseData.update(caseDataData, new ObjectMapper());
        caseData.setCaseDeadline(caseDeadline);
        caseData.setCaseDeadlineWarning(caseDeadlineWarning);

        when(caseDataService.getCaseDataField(caseUUID,overrideKey)).thenReturn(overrideDeadline.toString());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);
        when(extensionService.hasExtensions(caseUUID)).thenReturn(true);
        when(infoClient.getStageDeadlineOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadline())).thenReturn(caseData.getCaseDeadline());
        when(infoClient.getStageDeadlineWarningOverridingSLA(stageType, caseData.getDateReceived(), caseData.getCaseDeadlineWarning())).thenReturn(caseData.getCaseDeadlineWarning());

        // WHEN
        StageWithCaseData stage = stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        // THEN
        assertThat(stage.getDeadline()).isEqualTo(overrideDeadline);
        assertThat(stage.getDeadlineWarning()).isNull();
    }

    @Test
    public void shouldAuditCreateStage() {

        CaseData caseData = new CaseData(caseDataType, 12344567L, LocalDate.now());
        when(caseDataService.getCase(caseUUID)).thenReturn(caseData);

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        verify(auditClient).createStage(any(StageWithCaseData.class));
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
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.recreateStage(caseUUID, stageUUID, stageType);

        verify(stageRepository).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(auditClient).recreateStage(stage);

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
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageByCaseReferenceWithMissingReference() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageRepository.findByCaseReference(null)).thenReturn(Collections.singleton(stage));

        stageService.getActiveStagesByCaseReference(null);

        verify(stageRepository).findByCaseReference(null);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);

    }

    @Test
    public void shouldGetStageWithValidParams() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

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
    public void shouldGetActiveStages_blankResult() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());
        Set<String> caseTypes = Set.of();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUsersTeamsAndCaseType();

        verify(stageRepository).findAllActiveByTeamUUIDAndCaseType(teams, caseTypes);

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
    }

    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();
        Set<String> caseTypes = Set.of("CASE_TYPE1", "CASE_TYPE2");
        teams.add(UUID.randomUUID());
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(userPermissionsService.getUserTeams()).thenReturn(teams);
        when(userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(caseTypes);
        when(stageRepository.findAllActiveByTeamUUIDAndCaseType(teams, caseTypes)).thenReturn(Set.of(stage));

        stageService.getActiveStagesForUsersTeamsAndCaseType();

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

        stageService.getActiveStagesForUsersTeamsAndCaseType();

        // We don't try and get active stages with no teams (empty set) because we're going to get 0 results.
        verify(userPermissionsService).getUserTeams();
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
    public void shouldUpdateStageTeam() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
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
        StageWithCaseData repositoryStage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
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

        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        StageWithCaseData stage_old = new StageWithCaseData(UUID.randomUUID(), "DCU_MIN_MARKUP", null, null, transitionNoteUUID);
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
        verifyZeroInteractions(stageRepository);

    }

    @Test
    public void shouldGetOfflineQaUser() {
        UUID offlineQaUserUUID = UUID.randomUUID();
        Map dataMap = new HashMap();
        dataMap.put(StageWithCaseData.OFFLINE_QA_USER, offlineQaUserUUID.toString());
        final String offlineQaUser = stageService.getOfflineQaUser(Jackson.toJsonString(dataMap));
        assertThat(offlineQaUser).isEqualTo(offlineQaUserUUID.toString());
    }

    @Test
    public void shouldCheckSendOfflineQAEmail() {
        UUID offlineQaUserUUID = UUID.randomUUID();
        StageWithCaseData stage = createStageOfflineQaData(offlineQaUserUUID);
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
        ActiveStage activeStage1 = new ActiveStage(1L,
                UUID.randomUUID(), LocalDateTime.now(), "MPAM", LocalDate.now(), LocalDate.now(),
                UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        ActiveStage activeStage2 = new ActiveStage(2L,
                UUID.randomUUID(), LocalDateTime.now(), "MPAM", LocalDate.now(), LocalDate.now(),
                UUID.randomUUID(), caseUUID, teamUUID, UUID.randomUUID());
        StageWithCaseData stage1 = new StageWithCaseData(caseUUID, "stageType1", teamUUID, userUUID, transitionNoteUUID);
        StageWithCaseData stage2 = new StageWithCaseData(caseUUID, "stageType2", teamUUID, userUUID, transitionNoteUUID);

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

    @Test
    public void shouldGetActiveStagesByTeamUuids() {
        StageWithCaseData stage1 = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        StageWithCaseData stage2 = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);
        Set<StageWithCaseData> stages = Set.of(stage1, stage2);

        when(stageRepository.findAllActiveByTeamUUID(teamUUID)).thenReturn(stages);
        stageService.getActiveStagesByTeamUUID(teamUUID);
        verify(contributionsProcessor).processContributionsForStages(stages);
    }

    @Test
    public void shouldGetActiveUserStagesWithTeamsAndCaseType() {
        Set<UUID> teams = new HashSet<>();
        Set<String> caseTypes = Set.of("CASE_TYPE1", "CASE_TYPE2");
        teams.add(UUID.randomUUID());
        StageWithCaseData stage = new StageWithCaseData(caseUUID, "DCU_MIN_MARKUP", teamUUID, userUUID, transitionNoteUUID);

        when(userPermissionsService.getUserTeams()).thenReturn(teams);
        when(userPermissionsService.getCaseTypesIfUserTeamIsCaseTypeAdmin()).thenReturn(caseTypes);
        when(stageRepository.findAllActiveByUserUuidAndTeamUuidAndCaseType(userUUID, teams, caseTypes)).thenReturn(Set.of(stage));

        stageService.getActiveUserStagesWithTeamsAndCaseType(userUUID);

        verify(userPermissionsService).getUserTeams();
        verify(userPermissionsService).getCaseTypesIfUserTeamIsCaseTypeAdmin();
        verify(stageRepository).findAllActiveByUserUuidAndTeamUuidAndCaseType(userUUID, teams, caseTypes);
        verify(stagePriorityCalculator).updatePriority(stage);
        verify(daysElapsedCalculator).updateDaysElapsed(stage);

        checkNoMoreInteraction();
    }

    @Test
    public void shouldGetActiveUserStagesWithTeamsAndCaseType_noTeams() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveUserStagesWithTeamsAndCaseType(userUUID);

        verify(userPermissionsService).getUserTeams();
        checkNoMoreInteraction();
    }

    @Test
    public void getActiveUserStagesWithTeamsAndCaseType_blankResult() {
        Set<UUID> teams = Set.of(UUID.randomUUID());
        Set<String> caseTypes = Set.of();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveUserStagesWithTeamsAndCaseType(userUUID);

        verify(stageRepository).findAllActiveByUserUuidAndTeamUuidAndCaseType(userUUID, teams, caseTypes);

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
        assertThat(result.toString()).isEqualTo(teamUUID.toString());

        verify(stageRepository).findBasicStageByCaseUuidAndStageUuid(caseUuid, stageUuid);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void getStageTeam_nullResult() {
        var caseUuid = UUID.randomUUID();
        var stageUuid = UUID.randomUUID();

        when(stageRepository.findBasicStageByCaseUuidAndStageUuid(any(), any())).thenReturn(null);

        var result = stageService.getStageTeam(caseUuid, stageUuid);
        assertThat(result).isNull();

        verify(stageRepository).findBasicStageByCaseUuidAndStageUuid(caseUuid, stageUuid);

        verifyNoMoreInteractions(stageRepository);
    }

    /**
     * The stage cannot be an instance as it does not have a function to set data (in the Stage Class).
     * I did not want to create a setData on the Stage class for testing only.
     *
     * @return Mocked Stage for setting and exposing the DATA with offline QA user.
     */
    private StageWithCaseData createStageOfflineQaData(UUID offlineQaUserUUID) {
        Map dataMap = new HashMap();
        dataMap.put(StageWithCaseData.OFFLINE_QA_USER, offlineQaUserUUID.toString());
        StageWithCaseData mockStage = mock(StageWithCaseData.class);
        when(mockStage.getUuid()).thenReturn(stageUUID);
        when(mockStage.getCaseUUID()).thenReturn(caseUUID);
        when(mockStage.getStageType()).thenReturn(StageWithCaseData.DCU_DTEN_INITIAL_DRAFT);
        when(mockStage.getCaseReference()).thenReturn("MIN/1234567/19");
        when(mockStage.getData()).thenReturn(Jackson.toJsonString(dataMap));
        return mockStage;
    }

    private Set<GetAuditResponse> getAuditLines(StageWithCaseData stage) {
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
