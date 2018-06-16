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

    //@Captor
    //private ArgumentCaptor<AuditEntry> auditEntryArgumentCaptor;


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
    public void shouldCreateCaseCreateException() throws EntityCreationException {
        caseDataService.createCase(null, testUser);
    }

    @Test
    public void shouldCreateStage() throws EntityCreationException {
        StageData stageData = caseDataService.createStage(uuid, "CREATE", new HashMap<>(), testUser);

        verify(auditService).writeCreateStageEvent(testUser, stageData);
        verify(stageDataRepository).save(isA(StageData.class));

        assertThat(stageData).isNotNull();
        assertThat(stageData.getType()).isEqualTo("CREATE");
        assertThat(stageData.getCaseUUID()).isEqualTo(uuid);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingUUIDException() throws EntityCreationException {
        StageData stageData = caseDataService.createStage(null, "CREATE", new HashMap<>(), testUser);

        verify(auditService).writeCreateStageEvent(testUser, stageData);
        verify(stageDataRepository).save(isA(StageData.class));

        assertThat(stageData).isNotNull();
        assertThat(stageData.getType()).isEqualTo("CREATE");
        assertThat(stageData.getCaseUUID()).isEqualTo(uuid);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateStageMissingTypeException() throws EntityCreationException {
        StageData stageData = caseDataService.createStage(uuid, null, new HashMap<>(), testUser);

        verify(auditService).writeCreateStageEvent(testUser, stageData);
        verify(stageDataRepository).save(isA(StageData.class));

        assertThat(stageData).isNotNull();
        assertThat(stageData.getType()).isEqualTo("CREATE");
        assertThat(stageData.getCaseUUID()).isEqualTo(uuid);
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataRepository.findByUuid(any())).thenReturn(new StageData(
                uuid,
                "CREATE",
                "Some data"
        ));

        StageData stageData = caseDataService.updateStage(
                caseUUID,
                uuid,
                "CREATE",
                new HashMap<>(),
                testUser
        );

        assertThat(stageData).isNotNull();
        verify(stageDataRepository).findByUuid(uuid);
        verify(stageDataRepository).save(isA(StageData.class));

        verify(auditService).writeUpdateStageEvent(testUser, stageData);
/*        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(StageDataAudit.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());*/
    }

}
