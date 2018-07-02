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
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.email.EmailService;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;

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


    @Before
    public void setUp() {

        this.rshCaseService = new RshCaseService(
                caseDataService,
                emailService,
                "",
                ""
        );
    }

    @Test
    public void shouldCreateRshCase() throws EntityCreationException {
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        when(caseDataService.createCase(CaseType.RSH)).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        CaseData caseDataReturn = rshCaseService.createRshCase(
                data,
                sendEmailRequest);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).createCase(CaseType.RSH);
        verify(caseDataService, times(1)).createStage(caseData.getUuid(), StageType.DCU_MIN_CATEGORISE, data);
        verify(emailService, times(1)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateRshCaseNullData1() throws EntityCreationException {

        rshCaseService.createRshCase(
                null,
                new SendRshEmailRequest());
    }

    @Test
    public void shouldCreateRshCaseNullData2() throws EntityCreationException {

        try {
            rshCaseService.createRshCase(
                    null,
                    new SendRshEmailRequest());
        } catch (EntityCreationException e) {
            //Do nothing.
        }

        verify(caseDataService, times(0)).createCase(CaseType.MIN);
        verify(caseDataService, times(0)).createStage(any(UUID.class), any(StageType.class), anyMap());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test
    public void shouldCreateRshCaseNullEmail() throws EntityCreationException {
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        when(caseDataService.createCase(CaseType.RSH)).thenReturn(caseData);

        CaseData caseDataReturn = rshCaseService.createRshCase(
                new HashMap<>(),
                null);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).createCase(CaseType.RSH);
        verify(caseDataService, times(1)).createStage(caseData.getUuid(), StageType.DCU_MIN_CATEGORISE, new HashMap<>());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test
    public void shouldUpdateRshCase() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData(UUID.randomUUID(), StageType.DCU_MIN_CATEGORISE.toString(), "");
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        when(caseDataService.updateStage(any(UUID.class), any(UUID.class), any(StageType.class), anyMap())).thenReturn(stageData);

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
        verify(caseDataService, times(1)).updateStage(caseData.getUuid(), stageData.getUuid(), StageType.DCU_MIN_CATEGORISE, data);
        verify(emailService, times(1)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNullUUID1() throws EntityCreationException, EntityNotFoundException {

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        rshCaseService.updateRshCase(
                null,
                data,
                sendEmailRequest);
    }

    @Test
    public void shouldUpdateRshCaseNullUUID2() throws EntityCreationException, EntityNotFoundException {

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        try {
            rshCaseService.updateRshCase(
                    null,
                    data,
                    sendEmailRequest);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataService, times(0)).getCase(any());
        verify(caseDataService, times(0)).updateStage(any(), any(), any(), any());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNoStage1() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest);

    }

    @Test
    public void shouldUpdateRshCaseNoStage2() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        try {
            rshCaseService.updateRshCase(
                    caseData.getUuid(),
                    data,
                    sendEmailRequest);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataService, times(1)).getCase(any());
        verify(caseDataService, times(0)).updateStage(any(), any(), any(), any());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNullDat1() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);

        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        rshCaseService.updateRshCase(
                caseData.getUuid(),
                null,
                sendEmailRequest);
    }

    @Test
    public void shouldUpdateRshCaseNullDat2() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);

        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        try {
            rshCaseService.updateRshCase(
                    caseData.getUuid(),
                    null,
                    sendEmailRequest);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataService, times(0)).getCase(caseData.getUuid());
        verify(caseDataService, times(0)).updateStage(caseData.getUuid(), stageData.getUuid(), StageType.DCU_MIN_CATEGORISE, null);
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test()
    public void shouldUpdateRshCaseNullEmail() throws EntityCreationException, EntityNotFoundException {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        when(caseDataService.updateStage(any(UUID.class), any(UUID.class), any(StageType.class), anyMap())).thenReturn(stageData);

        Map<String, String> data = new HashMap<>();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                null);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
        verify(caseDataService, times(1)).updateStage(caseData.getUuid(), stageData.getUuid(), StageType.DCU_MIN_CATEGORISE, data);
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test
    public void shouldGetCase() throws EntityNotFoundException {
        StageData stageData = new StageData(UUID.randomUUID(), "", "");
        CaseData caseData = new CaseData(CaseType.RSH.toString(), 1L);
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        CaseData caseDataReturn = rshCaseService.getRSHCase(caseData.getUuid());

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNullUUID1() throws EntityNotFoundException {
        rshCaseService.getRSHCase(null);
    }

    @Test
    public void shouldGetCaseNullUUI2() throws EntityNotFoundException {
        try {
            rshCaseService.getRSHCase(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataService, times(0)).getCase(any(UUID.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNullUReturn() throws EntityNotFoundException {
        when(caseDataService.getCase(any(UUID.class))).thenReturn(null);

        rshCaseService.getRSHCase(UUID.randomUUID());
    }

    @Test
    public void shouldGetCaseNullUReturn2() throws EntityNotFoundException {
        when(caseDataService.getCase(any(UUID.class))).thenReturn(null);

        try {
            rshCaseService.getRSHCase(UUID.randomUUID());
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataService, times(1)).getCase(any(UUID.class));
    }
}
