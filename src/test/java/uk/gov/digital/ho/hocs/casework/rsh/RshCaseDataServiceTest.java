package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.audit.AuditService;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.caseDetails.StageDataRepository;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RshCaseDataServiceTest {


    @Mock
    private AuditService auditService;
    @Mock
    private CaseDataRepository caseDataRepository;
    @Mock
    private StageDataRepository stageDataRepository;
    @Mock
    private CaseDataService caseDataService;
    @Mock
    private EmailService emailService;

    private RshCaseService rshCaseService;

    @Before
    public void setUp() {

        this.caseDataService = new CaseDataService(
                caseDataRepository,
                stageDataRepository,
                auditService
        );

        this.rshCaseService = new RshCaseService(
                caseDataService,
                emailService
        );
    }

    @Test
    public void shouldCreateRshCase() throws EntityCreationException {
        when(caseDataRepository.getNextSeriesId()).thenReturn(123L);
        CaseData caseData = rshCaseService.createRshCase(
                new HashMap<>(),
                new SendEmailRequest(
                        "SomeTestEmail@SomeDomain.com",
                        "Some Team Name"
                ),
                "Test User"
        );

        assertThat(caseData).isNotNull();
        verify(caseDataRepository).save(isA(CaseData.class));
        verify(stageDataRepository).save(isA(StageData.class));
        verify(auditService, times(1)).writeCreateCaseEvent("Test User", caseData);
        verify(auditService, times(1)).writeCreateStageEvent(anyString(), any(StageData.class));
    }
}
