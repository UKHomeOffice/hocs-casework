package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.*;
import uk.gov.digital.ho.hocs.casework.notify.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.search.SearchRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseServiceTest {



    @Mock
    private AuditStageDetailsRepository auditStageDetailsRepository;

    @Mock
    private AuditCaseDetailsRepository auditCaseDetailsRepository;

    @Mock
    private AuditRepository auditRepository;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;
    @Mock
    private StageDetailsRepository stageDetailsRepository;
    @Mock
    private NotifyService notifyService;

    private CaseService caseService;

    private final String testUser = "Test User";
    private final UUID uuid = UUID.randomUUID();

    @Captor
    private ArgumentCaptor<AuditEntry> auditEntryArgumentCaptor;


    @Before
    public void setUp() {
        this.caseService = new CaseService(
                notifyService,
                caseDetailsRepository,
                stageDetailsRepository,
                auditRepository
        );
    }

    @Test
    public void shouldCreateRshCase() {
        when(caseDetailsRepository.getNextSeriesId()).thenReturn(123L);
        CaseDetails caseDetails = caseService.createRshCase(
                new HashMap<>(),
                new NotifyRequest(
                        "SomeTestEmail@SomeDomain.com",
                        "Some Team Name"
                ),
                testUser
        );

        assertThat(caseDetails).isNotNull();
        verify(caseDetailsRepository).save(isA(CaseDetails.class));
        verify(stageDetailsRepository).save(isA(StageDetails.class));
        verify(auditRepository, times(2)).save(isA(AuditEntry.class));
    }

    @Test
    public void shouldCreateCase() {
        final Long caseID = 12345L;

        when(caseDetailsRepository.getNextSeriesId()).thenReturn(caseID);
        CaseDetails caseDetails = caseService.createCase("Type", testUser);

        assertThat(caseDetails).isNotNull();
        verify(caseDetailsRepository).save(isA(CaseDetails.class));

        verify(auditRepository).save(isA(AuditEntry.class));
        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNotNull().isInstanceOf(AuditCaseData.class);
        assertThat(auditEntry.getStageInstance()).isNull();
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getCaseInstance().getUuid()).isNotNull().isInstanceOf(UUID.class);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_CASE.toString());
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

        verify(auditRepository).save(isA(AuditEntry.class));
        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(AuditStageData.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.CREATE_STAGE.toString());
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

        verify(auditRepository).save(isA(AuditEntry.class));
        verify(auditRepository).save(auditEntryArgumentCaptor.capture());
        AuditEntry auditEntry = auditEntryArgumentCaptor.getValue();
        assertThat(auditEntry).isNotNull();
        assertThat(auditEntry.getUsername()).isEqualTo(testUser);
        assertThat(auditEntry.getCaseInstance()).isNull();
        assertThat(auditEntry.getStageInstance()).isNotNull().isInstanceOf(AuditStageData.class);
        assertThat(auditEntry.getCreated()).isNotNull().isInstanceOf(LocalDateTime.class);
        assertThat(auditEntry.getQueryData()).isNull();
        assertThat(auditEntry.getStageInstance().getCaseUUID()).isEqualTo(uuid);
        assertThat(auditEntry.getEventAction()).isEqualTo(AuditAction.UPDATE_STAGE.toString());
    }

}
