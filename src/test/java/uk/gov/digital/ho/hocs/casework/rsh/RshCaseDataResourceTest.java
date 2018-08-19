package uk.gov.digital.ho.hocs.casework.rsh;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseRequest;
import uk.gov.digital.ho.hocs.casework.rsh.dto.CreateRshCaseResponse;

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
    public void shouldCreateCase()  {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);
        CreateRshCaseRequest request = new CreateRshCaseRequest();

        when(mockRshCaseService.createRshCase(anyMap(), any())).thenReturn(caseData);

        ResponseEntity<CreateRshCaseResponse> response = rshCaseResource.rshCreateCase(request);

        verify(mockRshCaseService, times(1)).createRshCase(request.getCaseData(), request.getSendEmailRequest());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldUpdateCase() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);

        when(mockRshCaseService.updateRshCase(any(UUID.class), anyMap(), any())).thenReturn(caseData);

        CreateRshCaseRequest request = new CreateRshCaseRequest();
        ResponseEntity<CreateRshCaseResponse> response = rshCaseResource.rshUpdateCase(caseData.getUuid(), request);

        verify(mockRshCaseService, times(1)).updateRshCase(caseData.getUuid(), request.getCaseData(), request.getSendEmailRequest());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {
        CaseData caseData = new CaseData(CaseType.MIN, 0l);

        when(mockRshCaseService.getRSHCase(any(UUID.class))).thenReturn(caseData);

        ResponseEntity<CaseData> response = rshCaseResource.rshGetCase(caseData.getUuid());

        verify(mockRshCaseService, times(1)).getRSHCase(caseData.getUuid());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}