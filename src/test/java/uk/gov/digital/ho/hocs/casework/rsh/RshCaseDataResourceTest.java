package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseRequest;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RshCaseDataResourceTest {

    @Mock
    private RshCaseService mockRshCaseService;

    private RshCaseResource rshCaseResource;


    @Before
    public void setUp() {
        this.rshCaseResource = new RshCaseResource(mockRshCaseService);
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);
      
        when(mockRshCaseService.createRshCase(anyMap(), any())).thenReturn(caseData);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateCaseResponse> response = rshCaseResource.rshCreateCase(request);

        verify(mockRshCaseService, times(1)).createRshCase(request.getCaseData(), request.getSendEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCaseReference()).isEqualTo(caseData.getReference());
        assertThat(response.getBody().getUuid()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void shouldCreateCaseCreateException() throws EntityCreationException {
        when(mockRshCaseService.createRshCase(anyMap(), any())).thenThrow(EntityCreationException.class);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateCaseResponse> response = rshCaseResource.rshCreateCase(request);

        verify(mockRshCaseService, times(1)).createRshCase(request.getCaseData(), request.getSendEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {

        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);

        when(mockRshCaseService.updateRshCase(any(UUID.class), anyMap(), any())).thenReturn(caseData);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateCaseResponse> response = rshCaseResource.rshUpdateCase(caseData.getUuid(), request);

        verify(mockRshCaseService, times(1)).updateRshCase(caseData.getUuid(), request.getCaseData(), request.getSendEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getCaseReference()).isEqualTo(caseData.getReference());
        assertThat(response.getBody().getUuid()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void shouldUpdateCaseCreateException() throws EntityCreationException, EntityNotFoundException {
        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);

        when(mockRshCaseService.updateRshCase(any(UUID.class), anyMap(), any())).thenThrow(EntityCreationException.class);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateCaseResponse> response = rshCaseResource.rshUpdateCase(caseData.getUuid(), request);

        verify(mockRshCaseService, times(1)).updateRshCase(caseData.getUuid(), request.getCaseData(), request.getSendEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCaseFoundException() throws EntityCreationException, EntityNotFoundException {

        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);

        when(mockRshCaseService.updateRshCase(any(UUID.class), anyMap(), any())).thenThrow(EntityNotFoundException.class);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateCaseResponse> response = rshCaseResource.rshUpdateCase(caseData.getUuid(), request);

        verify(mockRshCaseService, times(1)).updateRshCase(caseData.getUuid(), request.getCaseData(), request.getSendEmailRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shoulGetCase() throws EntityNotFoundException {
        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);
      
        when(mockRshCaseService.getRSHCase(any(UUID.class))).thenReturn(caseData);

        ResponseEntity<CaseData> response = rshCaseResource.rshGetCase(caseData.getUuid());

        verify(mockRshCaseService, times(1)).getRSHCase(caseData.getUuid());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getReference()).isEqualTo(caseData.getReference());
        assertThat(response.getBody().getUuid()).isEqualTo(caseData.getUuid());
    }

    @Test
    public void shoulGetCaseFoundException() throws EntityNotFoundException {

        CaseData caseData = new CaseData(UUID.randomUUID(),CaseType.RSH.toString(), 1L);
      
        when(mockRshCaseService.getRSHCase(any(UUID.class))).thenThrow(EntityNotFoundException.class);

        ResponseEntity<CaseData> response = rshCaseResource.rshGetCase(caseData.getUuid());

        verify(mockRshCaseService, times(1)).getRSHCase(caseData.getUuid());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
