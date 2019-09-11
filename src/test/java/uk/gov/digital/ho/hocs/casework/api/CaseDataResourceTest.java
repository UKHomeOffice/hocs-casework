package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
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
    private LocalDate caseReceived = LocalDate.now();
    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper, caseReceived);
        CreateCaseRequest request = new CreateCaseRequest(caseDataType.getDisplayCode(), data,caseReceived);

        when(caseDataService.createCase(caseDataType.getDisplayCode(), data, caseReceived)).thenReturn(caseData);

        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseDataType.getDisplayCode(), data, caseReceived);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCase() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper,caseReceived);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.of(Boolean.FALSE));

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseNull() {

        CaseData caseData = new CaseData(caseDataType, caseID, data, objectMapper,caseReceived);

        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.empty());

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
    public void shouldGetCaseSummary() {

        when(caseDataService.getCaseSummary(uuid)).thenReturn(new CaseSummary(null, null, null, null, null, null));

        ResponseEntity response = caseDataResource.getCaseSummary(uuid);

        verify(caseDataService, times(1)).getCaseSummary(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetCaseWithCorrespondentAndTopic() {

        Correspondent correspondent = new Correspondent(UUID.randomUUID(), "TYPE", "name",
                new Address("postcode","address1","address2","address3","county"),
                "phone", "email", "", "");

        Topic topic = new Topic(UUID.randomUUID(), "name", UUID.randomUUID());
        CaseData caseData = mock(CaseData.class);

        when(caseData.getPrimaryCorrespondent()).thenReturn(correspondent);
        when(caseData.getPrimaryTopic()).thenReturn(topic);
        when(caseData.getCreated()).thenReturn(LocalDateTime.of(2019,1,1,6,0));
        when(caseDataService.getCase(uuid)).thenReturn(caseData);

        ResponseEntity<GetCaseResponse> response = caseDataResource.getCase(uuid, Optional.of(Boolean.TRUE));

        verify(caseDataService, times(1)).getCase(uuid);

        verifyNoMoreInteractions(caseDataService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getPrimaryCorrespondent()).isInstanceOf(GetCorrespondentResponse.class);
        assertThat(response.getBody().getPrimaryTopic()).isInstanceOf(GetTopicResponse.class);
    }

    @Test
    public void shouldGetRegionUUIDForCase() {

        UUID regionUUID = UUID.randomUUID();

        when(caseDataService.getRegionUUIDForCase(uuid)).thenReturn(regionUUID);

        ResponseEntity<UUID> response = caseDataResource.getRegionUUIDForCase(uuid);

        verify(caseDataService, times(1)).getRegionUUIDForCase(uuid);
        verifyNoMoreInteractions(caseDataService);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(regionUUID);
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
