package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.notifiyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final String stageType = "DCU_MIN_MARKUP";
    private final LocalDate deadline = LocalDate.now();
    private final String allocationType = "anyAllocate";
    @Mock
    private StageRepository stageRepository;
    private StageService stageService;
    @Mock
    private UserPermissionsService userPermissionsService;
    @Mock
    private NotifyClient notifyClient;
    @Mock
    private CaseNoteService caseNoteService;
    @Mock
    private InfoClient infoClient;


    @Before
    public void setUp() {
        this.stageService = new StageService(stageRepository, caseNoteService, userPermissionsService, infoClient, notifyClient);
    }

    @Test
    public void shouldCreateStage() {

        stageService.createStage(caseUUID, stageType, teamUUID, deadline, allocationType);

        verify(stageRepository, times(1)).save(any(Stage.class));
        verify(notifyClient, times(1)).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldCreateStageNoDeadline() {

        stageService.createStage(caseUUID, stageType, teamUUID, null, allocationType);

        verify(stageRepository, times(1)).save(any(Stage.class));
        verify(notifyClient, times(1)).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);
    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateStageMissingCaseUUIDException() {

        stageService.createStage(null, stageType, teamUUID, deadline, null);
    }

    @Test()
    public void shouldNotCreateStageMissingCaseUUID() {

        try {
            stageService.createStage(null, stageType, teamUUID, deadline, null);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {

        stageService.createStage(caseUUID, null, teamUUID, deadline, null);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        try {
            stageService.createStage(caseUUID, null, teamUUID, deadline, null);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldGetStageWithValidParams() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.getActiveStage(caseUUID, stageUUID);

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

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

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

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

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(null, stageUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

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

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, null);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldGetActiveStagesCaseUUID() {

        stageService.getActiveStagesByCaseUUID(caseUUID);

        verify(stageRepository, times(1)).findAllActiveByCaseUUID(caseUUID);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();
        teams.add(UUID.randomUUID());

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUser();

        verify(stageRepository, times(1)).findAllActiveByTeamUUIDIn(teams);

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldGetActiveStagesEmpty() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStagesForUser();

        // We don't try and get active stages with no teams (empty set) because we're going to get 0 results.

        verifyZeroInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldUpdateStageDeadline() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageDeadline(caseUUID, stageUUID, deadline);

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldUpdateStageDeadlineNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageDeadline(caseUUID, stageUUID, null);

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
        verifyZeroInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

    @Test
    public void shouldUpdateStageTeam() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);
        Set<String> stageTypes = new HashSet<>();

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);
        when(infoClient.getCaseNoteStageTypes("DCU_MIN_MARKUP")).thenReturn(stageTypes);

        stageService.updateStageTeam(caseUUID, stageUUID, teamUUID, allocationType);

        verify(stageRepository, times(1)).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);
        verify(notifyClient, times(1)).sendTeamEmail(eq(caseUUID), any(UUID.class), eq(teamUUID), eq(null), eq(allocationType));
        verify(infoClient, times(1)).getCaseNoteStageTypes("DCU_MIN_MARKUP");
        verify(caseNoteService, times(1)).getLatestCaseNotes(caseUUID, stageTypes);

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseNoteService);

    }

    @Test
    public void shouldUpdateStageTeamNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);
        Set<String> stageTypes = new HashSet<>();

        when(stageRepository.findByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);
        when(infoClient.getCaseNoteStageTypes("DCU_MIN_MARKUP")).thenReturn(stageTypes);

        stageService.updateStageTeam(caseUUID, stageUUID, null, allocationType);

        verify(stageRepository, times(1)).findByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);
        verify(infoClient, times(1)).getCaseNoteStageTypes("DCU_MIN_MARKUP");
        verify(caseNoteService, times(1)).getLatestCaseNotes(caseUUID, stageTypes);

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyNoMoreInteractions(infoClient);
        verifyNoMoreInteractions(caseNoteService);

    }

    @Test
    public void shouldUpdateStageUser() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, userUUID);

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);
        verify(notifyClient, times(1)).sendUserEmail(eq(caseUUID), any(UUID.class), eq(null), eq(userUUID), eq(null));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);
    }

    @Test
    public void shouldUpdateStageUserNull() {

        Stage stage = new Stage(caseUUID, "DCU_MIN_MARKUP", teamUUID, deadline);

        when(stageRepository.findActiveByCaseUuidStageUUID(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStageUser(caseUUID, stageUUID, null);

        verify(stageRepository, times(1)).findActiveByCaseUuidStageUUID(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);
        verify(notifyClient, times(1)).sendUserEmail(eq(caseUUID), any(UUID.class), eq(null), eq(null), eq(null));

        verifyNoMoreInteractions(stageRepository);
        verifyNoMoreInteractions(notifyClient);
        verifyZeroInteractions(infoClient);
        verifyZeroInteractions(caseNoteService);

    }

}