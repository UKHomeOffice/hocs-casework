package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData.OFFLINE_QA_USER;

@RunWith(MockitoJUnitRunner.class)
@ActiveProfiles("local")
public class EmailServiceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    private final UUID userUUID = UUID.randomUUID();

    private final UUID offlineUUID = UUID.randomUUID();

    private EmailService emailService;

    @Mock
    private NotifyClient notifyClient;

    @Mock
    private CaseDataService caseDataService;

    @Before
    public void setUp() {
        this.emailService = new EmailService(notifyClient, caseDataService);
    }

    @Test
    public void shouldCheckSendOfflineQAEmail_DTEN() {
        var caseData = mock(CaseData.class);
        when(caseData.getReference()).thenReturn("Any Value");
        when(caseData.getData(OFFLINE_QA_USER)).thenReturn(offlineUUID.toString());
        when(caseDataService.getCaseInternal(caseUUID)).thenReturn(caseData);

        emailService.sendOfflineQAEmail(caseUUID, stageUUID, StageWithCaseData.DCU_DTEN_INITIAL_DRAFT, userUUID);

        verify(notifyClient).sendOfflineQaEmail(caseUUID, stageUUID, userUUID, offlineUUID, caseData.getReference());
    }

    @Test
    public void shouldCheckSendOfflineQAEmail_MIN() {
        var caseData = mock(CaseData.class);
        when(caseData.getReference()).thenReturn("Any Value");
        when(caseData.getData(OFFLINE_QA_USER)).thenReturn(offlineUUID.toString());
        when(caseDataService.getCaseInternal(caseUUID)).thenReturn(caseData);

        emailService.sendOfflineQAEmail(caseUUID, stageUUID, StageWithCaseData.DCU_MIN_INITIAL_DRAFT, userUUID);

        verify(notifyClient).sendOfflineQaEmail(caseUUID, stageUUID, userUUID, offlineUUID, caseData.getReference());
    }

    @Test
    public void shouldCheckSendOfflineQAEmail_TRO() {
        var caseData = mock(CaseData.class);
        when(caseData.getReference()).thenReturn("Any Value");
        when(caseData.getData(OFFLINE_QA_USER)).thenReturn(offlineUUID.toString());
        when(caseDataService.getCaseInternal(caseUUID)).thenReturn(caseData);

        emailService.sendOfflineQAEmail(caseUUID, stageUUID, StageWithCaseData.DCU_TRO_INITIAL_DRAFT, userUUID);

        verify(notifyClient).sendOfflineQaEmail(caseUUID, stageUUID, userUUID, offlineUUID, caseData.getReference());
    }

    @Test
    public void shouldCheckSendOfflineQAEmail_NullCurrentUser() {
        emailService.sendOfflineQAEmail(caseUUID, stageUUID, StageWithCaseData.DCU_DTEN_INITIAL_DRAFT, null);

        verifyNoInteractions(notifyClient);
    }

    @Test
    public void shouldCheckSendOfflineQAEmail_NoOfflineUser() {
        var caseData = mock(CaseData.class);
        when(caseDataService.getCaseInternal(caseUUID)).thenReturn(caseData);

        emailService.sendOfflineQAEmail(caseUUID, stageUUID, StageWithCaseData.DCU_DTEN_INITIAL_DRAFT, userUUID);

        verifyNoInteractions(notifyClient);
    }

}
