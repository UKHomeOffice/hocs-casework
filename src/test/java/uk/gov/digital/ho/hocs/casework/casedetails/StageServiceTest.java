package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageServiceTest {

    @Mock
    private StageDataRepository stageDataRepository;

    @Mock
    private AuditService auditService;

    private StageDataService stageDataService;

    @Before
    public void setUp() {
        this.stageDataService = new StageDataService(
                stageDataRepository,
                auditService);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageDataService.createStage(uuid, stageType, teamUUID, null);

        verify(stageDataRepository, times(1)).save(any(Stage.class));
        verify(auditService, times(1)).createStageEvent(uuid, stageType, teamUUID, null);

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(auditService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingUUIDException() {

        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageDataService.createStage(null, stageType, teamUUID, null);
    }

    @Test()
    public void shouldNotCreateStageMissingUUID() {

        StageType stageType = StageType.DCU_MIN_MARKUP;
        UUID teamUUID = UUID.randomUUID();

        try {
            stageDataService.createStage(null, stageType, teamUUID, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTypeException() {
        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        stageDataService.createStage(uuid, null, teamUUID, null);
    }

    @Test()
    public void shouldNotCreateStageMissingType() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        try {
            stageDataService.createStage(uuid, null, teamUUID, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotCreateStageMissingTeamUUIDException() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageDataService.createStage(uuid, stageType, null, null);
    }

    @Test()
    public void shouldNotCreateStageMissingTeamUUID() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        try {
            stageDataService.createStage(uuid, stageType, null, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldAllocateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, null);

        stageDataService.allocateStage(uuid, teamUUID, null);

        verify(stageDataRepository, times(1)).allocateToTeam(uuid, teamUUID);
        verify(auditService, times(1)).allocateStageEvent(uuid, teamUUID, null);

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(auditService);
    }

    @Test
    public void shouldAllocateStageWithValidParamsUserUUID() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        stageDataService.allocateStage(uuid, teamUUID, uuid);

        verify(stageDataRepository, times(1)).allocateToUser(uuid, teamUUID, uuid);
        verify(auditService, times(1)).allocateStageEvent(uuid, teamUUID, uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(auditService);
    }


    @Test(expected = EntityCreationException.class)
    public void shouldNotUpdateStageStageMissingTeamUUIDException() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        stageDataService.allocateStage(uuid, null, null);
    }

    @Test
    public void shouldNotUpdateStageStageMissingTeamUUID() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        try {
            stageDataService.allocateStage(uuid, null, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        Stage stage = new Stage(uuid, stageType, uuid, uuid);

        when(stageDataRepository.findByUuid(uuid)).thenReturn(stage);

        stageDataService.getStage(uuid);

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(auditService, times(1)).getStageEvent(uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(auditService);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageWithValidParamsNotFoundException() {

        UUID uuid = UUID.randomUUID();

        when(stageDataRepository.findByUuid(uuid)).thenReturn(null);

        stageDataService.getStage(uuid);
    }

    @Test
    public void shouldNotGetStageWithValidParamsNotFound() {

        UUID uuid = UUID.randomUUID();

        when(stageDataRepository.findByUuid(uuid)).thenReturn(null);

        try {
            stageDataService.getStage(uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageDataRepository, times(1)).findByUuid(uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotGetStageMissingUUIDException() {

        stageDataService.getStage(null);

    }

    @Test()
    public void shouldNotGetStageMissingUUID() {

        try {
            stageDataService.getStage(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(auditService);
    }

}
