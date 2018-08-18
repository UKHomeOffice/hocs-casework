package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.CaseDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.InputDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.StageDataService;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.rsh.dto.SendRshEmailRequest;
import uk.gov.digital.ho.hocs.casework.rsh.email.EmailService;
import uk.gov.digital.ho.hocs.casework.rsh.email.dto.SendEmailRequest;

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
    private InputDataService inputDataService;

    @Mock
    private StageDataService stageDataService;

    @Mock
    private EmailService emailService;

    private RshCaseService rshCaseService;


    @Before
    public void setUp() {

        this.rshCaseService = new RshCaseService(
                caseDataService,
                stageDataService,
                inputDataService,
                emailService,
                ""
        );
    }

    @Test
    public void shouldCreateRshCase()  {
        CaseData caseData = new CaseData(CaseType.RSH, 0l);
        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();

        when(caseDataService.createCase(eq(CaseType.RSH))).thenReturn(caseData);

        CaseData caseDataReturn = rshCaseService.createRshCase(
                data,
                sendEmailRequest);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).createCase(eq(CaseType.RSH));
        verify(stageDataService, times(1)).createStage(caseData.getUuid(), StageType.RUSH_ONLY_STAGE, null, null);
        verify(inputDataService, times(1)).createInputData(caseData.getUuid());
        verify(inputDataService, times(1)).updateInputData(caseData.getUuid(), data);
        verify(emailService, times(1)).sendRshEmail(any(SendEmailRequest.class));

        verifyNoMoreInteractions(caseDataService);
        verifyNoMoreInteractions(emailService);
        verifyNoMoreInteractions(stageDataService);
        verifyNoMoreInteractions(inputDataService);

    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateRshCaseNullData1() {

        rshCaseService.createRshCase(null, new SendRshEmailRequest());

    }

    @Test
    public void shouldCreateRshCaseNullData2() {

        try {
            rshCaseService.createRshCase(
                    null,
                    new SendRshEmailRequest());
        } catch (EntityCreationException e) {
            //Do nothing.
        }

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(emailService);
        verifyZeroInteractions(stageDataService);
        verifyZeroInteractions(inputDataService);
    }

    @Test(expected = EntityCreationException.class)
    public void shouldCreateRshCaseNullCaseCreate1()  {

        when(caseDataService.createCase(eq(CaseType.RSH))).thenReturn(null);

        rshCaseService.createRshCase(new HashMap<>(), new SendRshEmailRequest());
    }

    @Test
    public void shouldCreateRshCaseNullCaseCreate2()  {

        when(caseDataService.createCase(eq(CaseType.RSH))).thenReturn(null);
        CaseData caseData = new CaseData(CaseType.RSH, 0l);

        try {
            rshCaseService.createRshCase(new HashMap<>(), new SendRshEmailRequest());
        } catch (EntityCreationException e) {
            //Do nothing.
        }

        verify(caseDataService, times(1)).createCase(eq(CaseType.RSH));

        verifyNoMoreInteractions(caseDataService);
        verifyZeroInteractions(emailService);
        verifyZeroInteractions(stageDataService);
        verifyZeroInteractions(inputDataService);
    }

    @Test
    public void shouldCreateRshCaseNullEmail() {
        Map<String, String> data = new HashMap<>();
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        when(caseDataService.createCase(eq(CaseType.RSH))).thenReturn(caseData);

        CaseData caseDataReturn = rshCaseService.createRshCase(
                new HashMap<>(),
                null);

        verify(caseDataService, times(1)).createCase(eq(CaseType.RSH));
        verify(stageDataService, times(1)).createStage(caseData.getUuid(), StageType.RUSH_ONLY_STAGE, null, null);
        verify(inputDataService, times(1)).createInputData(caseData.getUuid());
        verify(inputDataService, times(1)).updateInputData(caseData.getUuid(), data);

        verifyNoMoreInteractions(caseDataService);
        verifyZeroInteractions(emailService);
        verifyNoMoreInteractions(stageDataService);
        verifyNoMoreInteractions(inputDataService);
    }

    @Test
    public void shouldUpdateRshCase() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        StageData stageData = new StageData(caseData.getUuid(), StageType.RUSH_ONLY_STAGE, UUID.randomUUID(), null);
        caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        //doNothing().when(stageDataService).removeActiveStage(any(UUID.class), any(UUID.class));

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
        //verify(stageDataService, times(1)).updateInputData(caseData.getUuid(), stageData.getUuid(), data);
        verify(emailService, times(1)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldNotUpdateRshCaseNullCaseUUID1() {

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        rshCaseService.updateRshCase(
                null,
                data,
                sendEmailRequest);
    }

    @Test
    public void shouldNotUpdateRshCaseNullCaseUUID2() {

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
        //verify(stageDataService, times(0)).removeActiveStage(any(), any());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNoStage1() {

        CaseData caseData = new CaseData(CaseType.MIN, 0l);

        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        Map<String, String> data = new HashMap<>();
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();
        rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                sendEmailRequest);

    }

    @Test
    public void shouldUpdateRshCaseNoStage2() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
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
        //verify(stageDataService, times(0)).removeActiveStage(any(), any());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test(expected = EntityCreationException.class)
    public void shouldUpdateRshCaseNullCaseData1() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();

        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);

        rshCaseService.updateRshCase(
                caseData.getUuid(),
                null,
                sendEmailRequest);
    }

    @Test
    public void shouldUpdateRshCaseNullCaseData2() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();

        when(caseDataService.getCase(caseData.getUuid())).thenReturn(caseData);

        try {
            rshCaseService.updateRshCase(
                    caseData.getUuid(),
                    null,
                    sendEmailRequest);
        } catch (EntityCreationException e) {
            // Do nothing.
        }

        verify(caseDataService, times(0)).getCase(caseData.getUuid());
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test()
    public void shouldUpdateRshCaseNullEmail() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        //StageData stageData = new StageData(caseData, StageType.RUSH_ONLY_STAGE, new HashSet<>());
        //caseData.getStages().add(stageData);
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        //doNothing().when(stageDataService).updateInputData(any(UUID.class), any(UUID.class), any());

        Map<String, String> data = new HashMap<>();
        CaseData caseDataReturn = rshCaseService.updateRshCase(
                caseData.getUuid(),
                data,
                null);

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
        //verify(stageDataService, times(1)).updateInputData(caseData.getUuid(), stageData.getUuid(), data);
        verify(emailService, times(0)).sendRshEmail(any(SendEmailRequest.class));
    }

    @Test
    public void shouldGetCase() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        //StageData stageData = new StageData(caseData, StageType.RUSH_ONLY_STAGE, new HashSet<>());
        //caseData.getStages().add(stageData);
      
        when(caseDataService.getCase(any(UUID.class))).thenReturn(caseData);

        CaseData caseDataReturn = rshCaseService.getRSHCase(caseData.getUuid());

        assertThat(caseDataReturn).isNotNull();

        verify(caseDataService, times(1)).getCase(caseData.getUuid());
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNullCaseUUID1()  {
        rshCaseService.getRSHCase(null);
    }

    @Test
    public void shouldGetCaseNullCaseUUI2() {
        try {
            rshCaseService.getRSHCase(null);
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataService, times(0)).getCase(any(UUID.class));
    }

    @Test(expected = EntityNotFoundException.class)
    public void shouldGetCaseNullReturn1()  {
        when(caseDataService.getCase(any(UUID.class))).thenReturn(null);

        rshCaseService.getRSHCase(UUID.randomUUID());
    }

    @Test
    public void shouldGetCaseNullReturn2() {
        when(caseDataService.getCase(any(UUID.class))).thenReturn(null);

        try {
            rshCaseService.getRSHCase(UUID.randomUUID());
        } catch (EntityNotFoundException e) {
            // Do nothing.
        }

        verify(caseDataService, times(1)).getCase(any(UUID.class));
    }
}
