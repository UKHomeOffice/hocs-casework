package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.domain.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;
import uk.gov.digital.ho.hocs.casework.domain.repository.StageRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageServiceTest {

    @Mock
    private StageRepository stageRepository;

    private StageService stageService;

    @Before
    public void setUp() {
        this.stageService = new StageService(
                stageRepository);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageService.createStage(uuid, stageType, teamUUID, null);

        verify(stageRepository, times(1)).save(any(Stage.class));

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingUUIDException() {

        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageService.createStage(null, stageType, teamUUID, null);
    }

    @Test()
    public void shouldNotCreateStageMissingUUID() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();

        try {
            stageService.createStage(null, stageType, teamUUID, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {
        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        stageService.createStage(uuid, null, teamUUID, null);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        try {
            stageService.createStage(uuid, null, teamUUID, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTeamUUIDException() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageService.createStage(uuid, stageType, null, null);
    }

    @Test()
    public void shouldNotCreateStageMissingTeamUUID() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        try {
            stageService.createStage(uuid, stageType, null, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageRepository);
    }

    @Test
    public void shouldAllocateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, null);

        stageService.allocateStage(uuid, uuid, teamUUID, null);

        verify(stageRepository, times(1)).allocateToTeam(uuid, uuid, teamUUID);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldAllocateStageWithValidParamsUserUUID() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        stageService.allocateStage(uuid, uuid, teamUUID, uuid);

        verify(stageRepository, times(1)).allocateToUser(uuid, uuid, teamUUID, uuid);

        verifyNoMoreInteractions(stageRepository);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotUpdateStageStageMissingTeamUUIDException() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        stageService.allocateStage(uuid, uuid, null, null);
    }

    @Test
    public void shouldNotUpdateStageStageMissingTeamUUID() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        try {
            stageService.allocateStage(uuid, uuid, null, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyNoMoreInteractions(stageRepository);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        when(stageRepository.findByUuid(uuid, uuid)).thenReturn(stage);

        stageService.getStage(uuid, uuid);

        verify(stageRepository, times(1)).findByUuid(uuid, uuid);

        verifyNoMoreInteractions(stageRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageWithValidParamsNotFoundException() {

        UUID uuid = UUID.randomUUID();

        when(stageRepository.findByUuid(uuid, uuid)).thenReturn(null);

        stageService.getStage(uuid, uuid);
    }

    @Test
    public void shouldNotGetStageWithValidParamsNotFound() {

        UUID uuid = UUID.randomUUID();

        when(stageRepository.findByUuid(uuid, uuid)).thenReturn(null);

        try {
            stageService.getStage(uuid, uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(uuid, uuid);

        verifyNoMoreInteractions(stageRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageMissingUUIDException() {

        stageService.getStage(null, null);

    }

    @Test()
    public void shouldNotGetStageMissingUUID() {

        try {
            stageService.getStage(null, null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageRepository, times(1)).findByUuid(null, null);

        verifyNoMoreInteractions(stageRepository);
    }

}
