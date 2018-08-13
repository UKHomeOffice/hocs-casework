package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.CaseInputDataRepository;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageDataServiceTest {

    @Mock
    private StageDataRepository stageDataRepository;

    @Mock
    private CaseInputDataRepository caseInputDataRepository;

    @Mock
    private ActiveStageService activeStageService;

    @Mock
    private AuditService auditService;

    private StageDataService stageDataService;

    @Before
    public void setUp() {
        this.stageDataService = new StageDataService(
                stageDataRepository,
                activeStageService,
                caseInputDataRepository,
                auditService);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        stageDataService.createStage(uuid, stageType, teamUUID, null);

        verify(stageDataRepository, times(1)).save(any(StageData.class));
        verify(activeStageService, times(1)).allocateStage(any(StageData.class));
        verify(auditService, times(1)).writeCreateStageEvent(any(StageData.class));

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(activeStageService);
        verifyNoMoreInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
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
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
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
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
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
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
    }

    @Test
    public void shouldAllocateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        StageData stageData = new StageData(uuid, stageType);

        when(stageDataRepository.findByUuid(uuid)).thenReturn(stageData);

        stageDataService.allocateStage(uuid, teamUUID, null);

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(stageDataRepository, times(1)).save(stageData);
        verify(activeStageService, times(1)).allocateStage(stageData);
        verify(auditService, times(1)).writeAllocateStageEvent(stageData);

        verifyNoMoreInteractions(stageDataRepository);
        verifyNoMoreInteractions(activeStageService);
        verifyNoMoreInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);

    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotAllocateStageWithValidParamsNotFoundException() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(uuid)).thenReturn(null);

        stageDataService.allocateStage(uuid, teamUUID, null);
    }

    @Test
    public void shouldNotAllocateStageWithValidParamsNotFound() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(uuid)).thenReturn(null);

        try {
            stageDataService.allocateStage(uuid, teamUUID, null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageDataRepository, times(1)).findByUuid(uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldNotUpdateStageMissingUUIDException() {

        UUID teamUUID = UUID.randomUUID();

        stageDataService.allocateStage(null, teamUUID, null);
    }

    @Test()
    public void shouldNotUpdateStageMissingUUID() {

        UUID teamUUID = UUID.randomUUID();

        try {
            stageDataService.allocateStage(null, teamUUID, null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(stageDataRepository, times(1)).findByUuid(null);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotUpdateStageStageMissingTeamUUIDException() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        StageData stageData = new StageData(uuid, stageType);

        when(stageDataRepository.findByUuid(uuid)).thenReturn(stageData);

        stageDataService.allocateStage(uuid, null, null);
    }

    @Test
    public void shouldNotUpdateStageStageMissingTeamUUID() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        StageData stageData = new StageData(uuid, stageType);

        when(stageDataRepository.findByUuid(uuid)).thenReturn(stageData);

        try {
            stageDataService.allocateStage(uuid, null, null);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(stageDataRepository, times(1)).findByUuid(uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;
        StageData stageData = new StageData(uuid, stageType);

        when(stageDataRepository.findByUuid(uuid)).thenReturn(stageData);

        stageDataService.getStage(uuid);

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(caseInputDataRepository, times(1)).findByCaseUUID(uuid);
        verify(auditService, times(1)).writeGetStageEvent(uuid);

        verifyNoMoreInteractions(stageDataRepository);
        verifyZeroInteractions(activeStageService);
        verifyNoMoreInteractions(auditService);
        verifyNoMoreInteractions(caseInputDataRepository);

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
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
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
        verifyZeroInteractions(activeStageService);
        verifyZeroInteractions(auditService);
        verifyZeroInteractions(caseInputDataRepository);
    }

}
