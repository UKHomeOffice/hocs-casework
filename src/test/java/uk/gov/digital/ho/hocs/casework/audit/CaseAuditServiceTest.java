package uk.gov.digital.ho.hocs.casework.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.RequestData;
import uk.gov.digital.ho.hocs.casework.audit.model.AuditEntry;
import uk.gov.digital.ho.hocs.casework.casedetails.model.UnitType;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseAuditServiceTest {

    @Mock
    private AuditService mockAuditService;

    @Mock
    private CaseAuditRepository mockCaseAuditRepository;

    @Mock
    private StageAuditRepository mockStageAuditRepository;

    @Mock
    private RequestData mockRequestData;

    private CaseAuditService caseAuditService;


    @Captor
    private ArgumentCaptor<AuditEntry> argCaptor;

    @Before
    public void setUp() {
        this.caseAuditService = new CaseAuditService(mockAuditService, mockCaseAuditRepository, mockStageAuditRepository, mockRequestData);
    }

    @Test
    public void shouldReturnEmptyWhenTenantIsNull() {
        String values = caseAuditService.getReportingDataAsCSV(null, LocalDate.MIN);
        assertThat(values).isEqualTo("");
    }

    @Test
    public void shouldReturnEmptyWhenCutoffIsNull() {
        String values = caseAuditService.getReportingDataAsCSV(UnitType.RSH, null);
        assertThat(values).isEqualTo("");
    }

    @Test
    public void shouldReturnEmptyWhenCutoffIsOutsideOfPossibleRangeMin() {
        String values = caseAuditService.getReportingDataAsCSV(UnitType.RSH, LocalDate.MIN);

        verify(mockAuditService, times(0)).writeExtractEvent(any());
        assertThat(values).isEqualTo("");
    }


    @Test
    public void shouldAuditValidAttempts() {
        String values = caseAuditService.getReportingDataAsCSV(UnitType.RSH, LocalDate.now());

        verify(mockAuditService, times(1)).writeExtractEvent(any());
    }

}
