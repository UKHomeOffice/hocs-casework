package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateCaseDataRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    private static final long caseID = 12345L;
    private final CaseDataType caseDataType = new CaseDataType("MIN", "a1");
    private final HashMap<String, String> data = new HashMap<>();
    private final UUID uuid = UUID.randomUUID();
    @Mock
    private CaseDataService caseDataService;
    private CaseDataResource caseDataResource;
    private ObjectMapper objectMapper = new ObjectMapper();
    private LocalDate caseDeadline = LocalDate.now().plusDays(20);
    private LocalDate caseReceived = LocalDate.now();
    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, caseDeadline, caseReceived);
        CreateCaseRequest request = new CreateCaseRequest(caseDataType, data, caseDeadline,caseReceived);

        when(caseDataService.createCase(caseDataType, data, caseDeadline, caseReceived)).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseDataType, data, caseDeadline,caseReceived);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, caseDeadline,caseReceived);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid);

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteCase() {

        doNothing().when(caseDataService).deleteCase(uuid);

        ResponseEntity response = caseDataResource.deleteCase(uuid);

        verify(caseDataService, times(1)).deleteCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldGetCaseType() {
        CaseDataType caseDataType = new CaseDataType("MIN", "a1");

        when(caseDataService.getCaseType(uuid)).thenReturn(caseDataType.getDisplayCode());

        ResponseEntity response = caseDataResource.getCaseType(uuid);

        verify(caseDataService, times(1)).getCaseType(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseSummary() {

        when(caseDataService.getCaseSummary(uuid)).thenReturn(new CaseSummary(null, null, null, null, null, null));

        ResponseEntity response = caseDataResource.getCaseSummary(uuid);

        verify(caseDataService, times(1)).getCaseSummary(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldUpdateCaseData() {
        UpdateCaseDataRequest updateCaseDataRequest = new UpdateCaseDataRequest(data);

        doNothing().when(caseDataService).updateCaseData(uuid, uuid, data);

        ResponseEntity response = caseDataResource.updateCaseData(uuid, uuid, updateCaseDataRequest);

        verify(caseDataService, times(1)).updateCaseData(uuid, uuid, data);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
