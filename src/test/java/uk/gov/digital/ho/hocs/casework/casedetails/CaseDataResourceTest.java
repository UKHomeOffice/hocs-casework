package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseInputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    @Mock
    private CaseDataService caseDataService;

    private CaseDataResource caseDataResource;

    private final UUID uuid = UUID.randomUUID();

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() {

        CaseType caseType = CaseType.MIN;
        CaseData caseData = new CaseData();
        CaseInputData caseInputData = new CaseInputData(uuid, caseType, 2l);
        caseData.setCaseInputData(caseInputData);
        CreateCaseRequest request = new CreateCaseRequest(caseType);

        when(caseDataService.createCase(any())).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseType caseType = CaseType.MIN;
        CaseData caseData = new CaseData();
        CaseInputData caseInputData = new CaseInputData(uuid, caseType, 2l);
        caseData.setCaseInputData(caseInputData);
        CreateCaseRequest request = new CreateCaseRequest(caseType);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid);

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
