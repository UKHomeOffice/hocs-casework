package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditEntry;
import uk.gov.digital.ho.hocs.casework.audit.AuditRepository;
import uk.gov.digital.ho.hocs.casework.notify.NotifyRequest;
import uk.gov.digital.ho.hocs.casework.notify.NotifyService;
import uk.gov.digital.ho.hocs.casework.rsh.RshCaseService;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RshCaseServiceTest {


    @Mock
    private AuditRepository auditRepository;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;
    @Mock
    private StageDetailsRepository stageDetailsRepository;
    @Mock
    private CaseService caseService;
    @Mock
    private NotifyService notifyService;

    private RshCaseService rshCaseService;

    @Before
    public void setUp() {

        this.caseService = new CaseService(
                caseDetailsRepository,
                stageDetailsRepository,
                auditRepository
        );

        this.rshCaseService = new RshCaseService(
                notifyService,
                caseService,
                caseDetailsRepository,
                stageDetailsRepository,
                auditRepository
        );
    }

    @Test
    public void shouldCreateRshCase() {
        when(caseDetailsRepository.getNextSeriesId()).thenReturn(123L);
        CaseDetails caseDetails = rshCaseService.createRshCase(
                new HashMap<>(),
                new NotifyRequest(
                        "SomeTestEmail@SomeDomain.com",
                        "Some Team Name"
                ),
                "Test User"
        );

        assertThat(caseDetails).isNotNull();
        verify(caseDetailsRepository).save(isA(CaseDetails.class));
        verify(stageDetailsRepository).save(isA(StageDetails.class));
        verify(auditRepository, times(2)).save(isA(AuditEntry.class));
    }
}
