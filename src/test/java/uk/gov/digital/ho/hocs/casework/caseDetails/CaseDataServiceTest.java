package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
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
    public void shouldCreateCase() {
        final Long caseID = 12345L;

        when(caseDataRepository.getNextSeriesId()).thenReturn(caseID);
        CaseData caseData = caseDataService.createCase("Type", testUser);

        assertThat(caseData).isNotNull();
        verify(caseDataRepository).save(isA(CaseData.class));

        verify(auditService).writeCreateCaseEvent(testUser, caseData);
/*        verify(auditService).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNotNull().isInstanceOf(CaseDataAudit.class);
        assertThat(auditEntry.getStageInstance()).isNull();
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getCaseInstance().getUuid()).isNotNull().isInstanceOf(UUID.class);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());*/
    }

    @Test
    public void shouldCreateStage() {
        StageData stageData = caseDataService.createStage(
                uuid,
                "CREATE",
                1,
                new HashMap<>(),
                testUser
        );

        assertThat(stageData).isNotNull();
        verify(stageDataRepository).save(isA(StageData.class));

        verify(auditService).writeCreateStageEvent(testUser, stageData);
/*        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(StageDataAudit.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());*/
    }

    @Test
    public void shouldUpdateStage() {
        when(stageDataRepository.findByUuid(any())).thenReturn(new StageData(
                uuid,
                "CREATE",
                1,
                "Some data"
        ));

        StageData stageData = caseDataService.updateStage(
                uuid,
                1,
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
