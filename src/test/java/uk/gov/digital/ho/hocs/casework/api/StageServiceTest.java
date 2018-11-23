package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;
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
    private final StageType stageType = StageType.DCU_MIN_MARKUP;
    private final LocalDate deadline = LocalDate.now();

    @Mock
    private StageRepository stageRepository;
    private StageService stageService;
    @Mock
    private UserPermissionsService userPermissionsService;

    @Before
    public void setUp() {
        this.stageService = new StageService(stageRepository, userPermissionsService);
    }

    @Test
    public void shouldCreateStage() {

        stageService.createStage(caseUUID, stageType, teamUUID, deadline);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldCreateStageNoDeadline() {

        stageService.createStage(caseUUID, stageType, teamUUID, null);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldCreateStageNoUser() {

        stageService.createStage(caseUUID, stageType, teamUUID, deadline);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingCaseUUIDException() {

        stageService.createStage(null, stageType, teamUUID, deadline);
    }

    @Test()
    public void shouldNotCreateStageMissingCaseUUID() {

        try {
            stageService.createStage(null, stageType, teamUUID, deadline);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {

        stageService.createStage(caseUUID, null, teamUUID, deadline);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        try {
            stageService.createStage(caseUUID, null, teamUUID, deadline);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.getStage(caseUUID, stageUUID);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageWithValidParamsNotFoundException() {

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(null);

        stageService.getStage(caseUUID, stageUUID);
    }

    @Test
    public void shouldNotGetStageWithValidParamsNotFound() {

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(null);

        try {
            stageService.getStage(caseUUID, stageUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageMissingCaseUUIDException() {

        stageService.getStage(null, stageUUID);
    }

    @Test()
    public void shouldNotGetStageMissingCaseUUID() {

        try {
            stageService.getStage(null, stageUUID);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(null, stageUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageMissingStageUUIDException() {

        stageService.getStage(caseUUID, null);
    }

    @Test()
    public void shouldNotGetStageMissingStageUUID() {

        try {
            stageService.getStage(caseUUID, null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(caseUUID, null);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetActiveStagesUserUUID() {

        stageService.getActiveStagesByUserUUID(userUUID);

        verify(stageRepository, times(1)).findAllByUserUUID(userUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetActiveStagesUserUUIDNull() {

        stageService.getActiveStagesByUserUUID(null);

        verify(stageRepository, times(1)).findAllByUserUUID(null);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetActiveStagesTeamUUID() {

        stageService.getActiveStagesByTeamUUID(teamUUID);

        verify(stageRepository, times(1)).findAllByTeamUUID(teamUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetActiveStagesTeamUUIDNull() {

        stageService.getActiveStagesByTeamUUID(null);

        verify(stageRepository, times(1)).findAllByTeamUUID(null);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetActiveStages() {
        Set<UUID> teams = new HashSet<>();

        when(userPermissionsService.getUserTeams()).thenReturn(teams);

        stageService.getActiveStages();

        verify(stageRepository, times(1)).findAllBy(teams);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldUpdateStageDeadline() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateDeadline(caseUUID, stageUUID, deadline);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldUpdateStageDeadlineException() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, null);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateDeadline(caseUUID, stageUUID, deadline);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }
}
