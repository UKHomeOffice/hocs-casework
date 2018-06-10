package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageDetails;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceTest {

    @Mock
    private AuditService auditService;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;
    @Mock
    private StageDetailsRepository stageDetailsRepository;

    private CaseService caseService;

    private final String testUser = "Test User";
    private final UUID uuid = UUID.randomUUID();

    //@Captor
    //private ArgumentCaptor<AuditEntry> auditEntryArgumentCaptor;


    @Before
    public void setUp() {
        this.caseService = new CaseService(
                caseDetailsRepository,
                stageDetailsRepository,
                auditService
        );
    }

    @Test
    public void shouldCreateCase() {
        final Long caseID = 12345L;

        when(caseDetailsRepository.getNextSeriesId()).thenReturn(caseID);
        CaseDetails caseDetails = caseService.createCase("Type", testUser);

        assertThat(caseDetails).isNotNull();
        verify(caseDetailsRepository).save(isA(CaseDetails.class));

        verify(auditService).writeCreateCaseEvent(testUser, caseDetails);
/*        verify(auditService).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNotNull().isInstanceOf(AuditCaseData.class);
        assertThat(auditEntry.getStageInstance()).isNull();
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getCaseInstance().getUuid()).isNotNull().isInstanceOf(UUID.class);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());*/
    }

    @Test
    public void shouldCreateStage() {
        StageDetails stageDetails = caseService.createStage(
                uuid,
                "CREATE",
                1,
                new HashMap<>(),
                testUser
        );

        assertThat(stageDetails).isNotNull();
        verify(stageDetailsRepository).save(isA(StageDetails.class));

        verify(auditService).writeCreateStageEvent(testUser, stageDetails);
/*        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(AuditStageData.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());*/
    }

    @Test
    public void shouldUpdateStage() {
        when(stageDetailsRepository.findByUuid(any())).thenReturn(new StageDetails(
                uuid,
                "CREATE",
                1,
                "Some data"
        ));

        StageDetails stageDetails = caseService.updateStage(
                uuid,
                1,
                new HashMap<>(),
                testUser
        );

        assertThat(stageDetails).isNotNull();
        verify(stageDetailsRepository).findByUuid(uuid);
        verify(stageDetailsRepository).save(isA(StageDetails.class));

        verify(auditService).writeUpdateStageEvent(testUser, stageDetails);
/*        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(AuditStageData.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());*/
    }

}
