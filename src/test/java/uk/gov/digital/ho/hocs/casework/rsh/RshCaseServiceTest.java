package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDetailsRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseService;
import uk.gov.digital.ho.hocs.casework.caseDetails.StageDetailsRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseDetails;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageDetails;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RshCaseServiceTest {


    @Mock
    private AuditService auditService;
    @Mock
    private CaseDetailsRepository caseDetailsRepository;
    @Mock
    private StageDetailsRepository stageDetailsRepository;
    @Mock
    private CaseService caseService;

    private RshCaseService rshCaseService;

    @Before
    public void setUp() {

        this.caseService = new CaseService(
                caseDetailsRepository,
                stageDetailsRepository,
                auditService
        );

        this.rshCaseService = new RshCaseService(
                caseService
        );
    }

    @Test
    public void shouldCreateRshCase() {
        when(caseDetailsRepository.getNextSeriesId()).thenReturn(123L);
        CaseDetails caseDetails = rshCaseService.createRshCase(
                new HashMap<>(),
                new SendEmailRequest(
                        "SomeTestEmail@SomeDomain.com",
                        "Some Team Name"
                ),
                "Test User"
        );

        assertThat(caseDetails).isNotNull();
        verify(caseDetailsRepository).save(isA(CaseDetails.class));
        verify(stageDetailsRepository).save(isA(StageDetails.class));
        verify(auditService, times(1)).writeCreateCaseEvent("Test User", caseDetails);
        verify(auditService, times(1)).writeCreateStageEvent(anyString(), any(StageDetails.class));
    }
}
