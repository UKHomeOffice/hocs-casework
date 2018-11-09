package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageServiceTest {

    @Mock
    private StageRepository stageRepository;

    private StageService stageService;

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final StageType stageType = StageType.DCU_MIN_MARKUP;
    private final LocalDate deadline = LocalDate.now();
    private final StageStatusType statusType = StageStatusType.CREATED;

    @Before
    public void setUp() {
        this.stageService = new StageService(stageRepository);
    }

    @Test
    public void shouldCreateStage() {

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, deadline);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldCreateStageNoDeadline() {

        stageService.createStage(caseUUID, stageType, teamUUID, userUUID, null);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldCreateStageNoUser() {

        stageService.createStage(caseUUID, stageType, teamUUID, null, deadline);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingCaseUUIDException() {

        stageService.createStage(null, stageType, teamUUID, null, deadline);
    }

    @Test()
    public void shouldNotCreateStageMissingCaseUUID() {

        try {
            stageService.createStage(null, stageType, teamUUID, userUUID, deadline);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {

        stageService.createStage(caseUUID, null, teamUUID, userUUID, deadline);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        try {
            stageService.createStage(caseUUID, null, teamUUID, userUUID, deadline);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

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
    public void shouldUpdateStage() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, userUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStage(caseUUID, stageUUID, teamUUID, userUUID, statusType);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldUpdateStageNoUser() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, userUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStage(caseUUID, stageUUID, teamUUID, null, statusType);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotUpdateStageMissingCaseUUIDException() {

        stageService.updateStage(null, stageUUID, teamUUID, userUUID, statusType);
    }

    @Test()
    public void shouldNotUpdateStageMissingCaseUUID() {

        try {
            stageService.updateStage(null, stageUUID, teamUUID, userUUID, statusType);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(null, stageUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotUpdateStageMissingStageException() {

        stageService.updateStage(caseUUID, null, teamUUID, userUUID, statusType);
    }

    @Test()
    public void shouldNotUpdateStageMissingStage() {

        try {
            stageService.updateStage(caseUUID, null, teamUUID, userUUID, statusType);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(caseUUID, null);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotUpdateStageMissingStatusTypeException() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.updateStage(caseUUID, stageUUID, teamUUID, userUUID, null);
    }

    @Test()
    public void shouldNotUpdateStageMissingStatusType() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);


        try {
            stageService.updateStage(caseUUID, stageUUID, teamUUID, userUUID, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);

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

        stageService.getActiveStages();

        verify(stageRepository, times(1)).findAllBy();

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldUpdateStageDeadline() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, userUUID, deadline);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.setDeadline(caseUUID, stageUUID, deadline);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldUpdateStageDeadlineException() {

        Stage stage = new Stage(caseUUID, StageType.DCU_MIN_MARKUP, teamUUID, userUUID, null);

        when(stageRepository.findByUuid(caseUUID, stageUUID)).thenReturn(stage);

        stageService.setDeadline(caseUUID, stageUUID, deadline);

        verify(stageRepository, times(1)).findByUuid(caseUUID, stageUUID);
        verify(stageRepository, times(1)).save(stage);

        verifyNoMoreInteractions(stageRepository);
    }
}
