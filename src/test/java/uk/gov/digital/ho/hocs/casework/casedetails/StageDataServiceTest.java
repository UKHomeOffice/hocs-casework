package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.StageDataRepository;

import java.util.HashMap;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageDataServiceTest {

    private final UUID uuid = UUID.randomUUID();
    @Mock
    private AuditService auditService;
    @Mock
    private StageDataRepository stageDataRepository;
    @Mock
    private ActiveStageService activeStageService;

    private StageDataService stageDataService;

    @Before
    public void setUp() {
        this.stageDataService = new StageDataService(
                stageDataRepository,
                activeStageService,
                auditService
        );
    }

    @Ignore
    @Test
    public void shouldCreateStage() throws EntityCreationException {
        stageDataService.createStage(uuid, StageType.DCU_MIN_MARKUP, new HashMap<>());

        verify(auditService).writeCreateStageEvent(any(StageData.class));
        verify(stageDataRepository).save(isA(StageData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingUUIDException1() throws EntityCreationException {
        stageDataService.createStage(null, StageType.DCU_MIN_MARKUP, new HashMap<>());
    }

    @Test()
    public void shouldCreateStageMissingUUIDException2() {
        try {
            stageDataService.createStage(null, StageType.DCU_MIN_MARKUP, new HashMap<>());
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingTypeException1() throws EntityCreationException {
        stageDataService.createStage(uuid, null, new HashMap<>());
    }

    @Test()
    public void shouldCreateStageMissingTypeException2() {
        try {
            stageDataService.createStage(uuid, null, new HashMap<>());
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Ignore
    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        when(stageDataRepository.findByUuid(any())).thenReturn(new StageData(uuid, StageType.DCU_MIN_MARKUP.toString(), "Some data"));

        stageDataService.completeStage(uuid, uuid);

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(stageDataRepository, times(1)).save(isA(StageData.class));
        verify(auditService, times(1)).writeUpdateStageEvent(any(StageData.class));

    }

    @Ignore
    @Test(expected = EntityCreationException.class)
    public void shouldUpdateStageMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        stageDataService.completeStage(caseUUID, null);
    }

    @Test()
    public void shouldUpdateStageMissingUUIDException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            stageDataService.completeStage(caseUUID, null);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(stageDataRepository, times(0)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any());
    }

    @Ignore
    @Test(expected = EntityNotFoundException.class)
    public void shouldUpdateStageNotFound1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        stageDataService.completeStage(caseUUID, uuid);
    }

    @Ignore
    @Test
    public void shouldUpdateStageNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        try {
            stageDataService.completeStage(caseUUID, uuid);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(stageDataRepository, times(1)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any());
    }

}
