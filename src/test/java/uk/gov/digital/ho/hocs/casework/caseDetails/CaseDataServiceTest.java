package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataServiceTest {

    @Mock
    private AuditService auditService;
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private StageDataRepository stageDataRepository;

    private CaseDataService caseDataService;

    private final String testUser = "Test User";
    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        this.caseDataService = new CaseDataService(
                caseDataRepository,
                stageDataRepository,
                auditService
        );
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);

        CaseData caseData = caseDataService.createCase("Type", testUser);

        verify(auditService, times(1)).writeCreateCaseEvent(testUser, caseData);
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));

        assertThat(caseData).isNotNull();
        assertThat(caseData.getType()).isEqualTo("Type");
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateCaseCreateException1() throws EntityCreationException {
        caseDataService.createCase(null, testUser);
    }

    @Test()
    public void shouldCreateCaseCreateException2() {
        try {
            caseDataService.createCase(null, testUser);
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateCaseEvent(anyString(), any(CaseData.class));
        verify(caseDataRepository, times(0)).save(any(CaseData.class));

    }

    @Test
    public void shouldCreateStage() throws EntityCreationException {
        StageData stageData = caseDataService.createStage(uuid, "CREATE", new HashMap<>(), testUser);

        verify(auditService).writeCreateStageEvent(testUser, stageData);
        verify(stageDataRepository).save(isA(StageData.class));

        assertThat(stageData).isNotNull();
        assertThat(stageData.getType()).isEqualTo("CREATE");
        assertThat(stageData.getData()).isEqualTo("{ }");
        assertThat(stageData.getCaseUUID()).isEqualTo(uuid);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingUUIDException1() throws EntityCreationException {
        caseDataService.createStage(null, "CREATE", new HashMap<>(), testUser);
    }

    @Test()
    public void shouldCreateStageMissingUUIDException2() {
        try {
            caseDataService.createStage(null, "CREATE", new HashMap<>(), testUser);
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any(), any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingTypeException1() throws EntityCreationException {
        caseDataService.createStage(uuid, null, new HashMap<>(), testUser);
    }

    @Test()
    public void shouldCreateStageMissingTypeException2() {
        try {
            caseDataService.createStage(uuid, null, new HashMap<>(), testUser);
        } catch (EntityCreationException e) {
            // Do nothing.
        }
        verify(auditService, times(0)).writeCreateStageEvent(any(), any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData("Type", 123L));

        CaseData caseData = caseDataService.updateCase(caseUUID, "Type", testUser);

        verify(caseDataRepository, times(1)).findByUuid(caseUUID);
        verify(caseDataRepository, times(1)).save(isA(CaseData.class));
        verify(auditService, times(1)).writeUpdateCaseEvent(testUser, caseData);

        assertThat(caseData).isNotNull();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateCaseMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        caseDataService.updateCase(null, "Type", testUser);
    }

    @Test()
    public void shouldUpdateCaseMissingUUIDException2() throws EntityNotFoundException {
        try {
            caseDataService.updateCase(null, "Type", testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any(), any());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateCaseMissingTypeException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>(), testUser);
    }

    @Test()
    public void shouldUpdateCaseMissingTypeException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateCase(caseUUID, null, testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldUpdateCaseNotFound1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.updateCase(caseUUID, "Type", testUser);
    }

    @Test
    public void shouldUpdateCaseNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.updateCase(caseUUID, "Type", testUser);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(caseDataRepository, times(0)).save(any(CaseData.class));
        verify(auditService, times(0)).writeUpdateCaseEvent(any(), any());
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(new StageData(uuid, "CREATE", "Some data"));

        StageData stageData = caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>(), testUser);

        verify(stageDataRepository, times(1)).findByUuid(uuid);
        verify(stageDataRepository, times(1)).save(isA(StageData.class));
        verify(auditService, times(1)).writeUpdateStageEvent(testUser, stageData);

        assertThat(stageData).isNotNull();
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateStageMissingUUIDException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, null, "CREATE", new HashMap<>(), testUser);
    }

    @Test()
    public void shouldUpdateStageMissingUUIDException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateStage(caseUUID, null, "CREATE", new HashMap<>(), testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(stageDataRepository, times(0)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateStageMissingTypeException1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>(), testUser);
    }

    @Test()
    public void shouldUpdateStageMissingTypeException2() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        try {
            caseDataService.updateStage(caseUUID, uuid, null, new HashMap<>(), testUser);
        } catch (EntityCreationException e) {
            // Do Nothing.
        }

        verify(stageDataRepository, times(0)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldUpdateStageNotFound1() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>(), testUser);
    }

    @Test
    public void shouldUpdateStageNotFound2() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.updateStage(caseUUID, uuid, "CREATE", new HashMap<>(), testUser);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }
        verify(stageDataRepository, times(1)).findByUuid(any());
        verify(stageDataRepository, times(0)).save(any(StageData.class));
        verify(auditService, times(0)).writeUpdateStageEvent(any(), any());
    }

    @Test
    public void shouldGetCase() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(new CaseData("", 1L));

        CaseData caseData = caseDataService.getCase(caseUUID, testUser);

        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(auditService, times(1)).writeGetCaseEvent(any(), any());

        assertThat(caseData).isNotNull();
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseMissingUUID1() throws EntityNotFoundException {
        caseDataService.getCase(null, testUser);
    }

    @Test
    public void shouldGetCaseMissingUUID2() {

        try {
            caseDataService.getCase(null, testUser);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(0)).findByUuid(any());
        verify(auditService, times(0)).writeGetCaseEvent(any(), any());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNotFoundException1() throws EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        caseDataService.getCase(caseUUID, testUser);
    }

    @Test
    public void shouldGetCaseNotFoundException2() {
        UUID caseUUID = UUID.randomUUID();

        when(caseDataRepository.findByUuid(any())).thenReturn(null);

        try {
            caseDataService.getCase(caseUUID, testUser);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataRepository, times(1)).findByUuid(any());
        verify(auditService, times(1)).writeGetCaseEvent(any(), any());
    }

}
