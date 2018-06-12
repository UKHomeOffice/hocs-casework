package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.caseDetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RshCaseDataServiceTest {

    @Mock
    private CaseDataService caseDataService;
    @Mock
    private EmailService emailService;

    private RshCaseService rshCaseService;

    private String testUser = "TestUser";

    @Before
    public void setUp() {

        this.rshCaseService = new RshCaseService(
                caseDataService,
                emailService
        );
    }

    @Test
    public void shouldCreateRshCase() throws EntityCreationException {
        CaseData caseData = new CaseData();
        when(caseDataService.createCase(anyString(), anyString())).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.createRshCase(
                data,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).createCase("RSH", testUser);
        verify(caseDataService, times(1)).createStage(caseData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(1)).sendRshEmail(sendEmailRequest, caseData.getUuid(), caseData.getReference(), null, testUser);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateRshCaseNullData() throws EntityCreationException {
        CaseData caseData = new CaseData();

        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.createRshCase(
                null,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(0)).createCase(anyString(), anyString());
        verify(caseDataService, times(0)).createStage(any(UUID.class), anyString(), anyMap(), anyString());
        verify(emailService, times(0)).sendRshEmail(null, any(UUID.class), anyString(), anyString(), anyString());
    }

    @Test()
    public void shouldCreateRshCaseNullEmail() throws EntityCreationException {
        CaseData caseData = new CaseData();
        when(caseDataService.createCase(anyString(), anyString())).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        CaseData caseDataReturn = rshCaseService.createRshCase(
                data,
                null,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).createCase("RSH", testUser);
        verify(caseDataService, times(1)).createStage(caseData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(1)).sendRshEmail(null, caseData.getUuid(), caseData.getReference(), null, testUser);
    }

    @Test
    public void shouldUpdateRshCase() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData();
        CaseData caseData = new CaseData();
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class), anyString())).thenReturn(caseData);

        when(caseDataService.updateStage(any(UUID.class), any(UUID.class), anyString(), anyMap(), anyString())).thenReturn(stageData);

        Map<String, String> data = new HashMap<>();
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid(), testUser);
        verify(caseDataService, times(1)).updateStage(caseData.getUuid(), stageData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(1)).sendRshEmail(sendEmailRequest, caseData.getUuid(), caseData.getReference(), null, testUser);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNullUUID() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData();
        StageData stageData = new StageData();

        Map<String, String> data = new HashMap<>();
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                null,
                data,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(0)).getCase(caseData.getUuid(), testUser);
        verify(caseDataService, times(0)).updateStage(caseData.getUuid(), stageData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(0)).sendRshEmail(null, any(UUID.class), anyString(), anyString(), anyString());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNoStages() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData();
        CaseData caseData = new CaseData();
        when(caseDataService.getCase(any(UUID.class), anyString())).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(0)).getCase(caseData.getUuid(), testUser);
        verify(caseDataService, times(0)).updateStage(caseData.getUuid(), stageData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(0)).sendRshEmail(null, any(UUID.class), anyString(), anyString(), anyString());
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNullData() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData();
        StageData stageData = new StageData();

        SendEmailRequest sendEmailRequest = new SendEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                null,
                sendEmailRequest,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(0)).getCase(caseData.getUuid(), testUser);
        verify(caseDataService, times(0)).updateStage(caseData.getUuid(), stageData.getUuid(), "Stage", null, testUser);
        verify(emailService, times(0)).sendRshEmail(null, any(UUID.class), anyString(), anyString(), anyString());
    }

    @Test()
    public void shouldUpdateRshCaseNullEmail() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData();
        CaseData caseData = new CaseData();
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class), anyString())).thenReturn(caseData);

        when(caseDataService.updateStage(any(UUID.class), any(UUID.class), anyString(), anyMap(), anyString())).thenReturn(stageData);

        Map<String, String> data = new HashMap<>();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                null,
                testUser);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid(), testUser);
        verify(caseDataService, times(1)).updateStage(caseData.getUuid(), stageData.getUuid(), "Stage", data, testUser);
        verify(emailService, times(1)).sendRshEmail(null, caseData.getUuid(), caseData.getReference(), null, testUser);
    }
}
