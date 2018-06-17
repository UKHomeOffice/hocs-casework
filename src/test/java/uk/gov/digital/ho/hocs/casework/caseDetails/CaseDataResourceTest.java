package uk.gov.digital.ho.hocs.casework.caseDetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.caseDetails.dto.*;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.caseDetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    @Mock
    private CaseDataService caseDataService;

    private CaseDataResource caseDataResource;

    private final UUID uuid = UUID.randomUUID();
    private final Map<String, String> data = new HashMap<>();
    private final String testUser = "Test User";

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final String caseType = "Case Type";

        when(caseDataService.createCase(any(), any())).thenReturn(new CaseData(caseType, 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request, testUser);

        verify(caseDataService, times(1)).createCase(caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody().getCaseReference()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateCaseResponse.class);
    }

    @Test
    public void shouldCreateCaseException() throws EntityCreationException {
        final String caseType = "Case Type";

        when(caseDataService.createCase(any(), any())).thenThrow(EntityCreationException.class);
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.createCase(request, testUser);

        verify(caseDataService, times(1)).createCase(caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        final String caseType = "Case Type";

        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.updateCase(any(), any(), any())).thenReturn(new CaseData(caseType, 1234L));
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(caseUUID, request, testUser);

        verify(caseDataService, times(1)).updateCase(caseUUID, caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateCaseCreateException() throws EntityCreationException, EntityNotFoundException {
        final String caseType = "Case Type";

        when(caseDataService.updateCase(any(), any(), any())).thenThrow(EntityCreationException.class);
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(uuid, request, testUser);

        verify(caseDataService, times(1)).updateCase(uuid, caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCaseFindException() throws EntityCreationException, EntityNotFoundException {
        final String caseType = "Case Type";

        when(caseDataService.updateCase(any(), any(), any())).thenThrow(EntityNotFoundException.class);
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(uuid, request, testUser);

        verify(caseDataService, times(1)).updateCase(uuid, caseType, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldCreateStage() throws EntityCreationException {
        String stageType = "Stage Type";
        Map<String, String> data = new HashMap<>();

        when(caseDataService.createStage(any(UUID.class), anyString(), anyMap(), anyString())).thenReturn(new StageData(uuid, stageType, ""));
        CreateStageRequest request = new CreateStageRequest(stageType, data);

        ResponseEntity<CreateStageResponse> response = caseDataResource.createStage(uuid, request, testUser);

        verify(caseDataService, times(1)).createStage(uuid, stageType, new HashMap<>(), testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateStageResponse.class);
    }

    @Test
    public void shouldCreateStageException() throws EntityCreationException {
        String stageType = "Stage Type";
        Map<String, String> data = new HashMap<>();

        when(caseDataService.createStage(any(UUID.class), anyString(), anyMap(), anyString())).thenThrow(EntityCreationException.class);
        CreateStageRequest request = new CreateStageRequest(stageType, data);

        ResponseEntity response = caseDataResource.createStage(uuid, request, testUser);

        verify(caseDataService, times(1)).createStage(uuid, stageType, new HashMap<>(), testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        final String stageType = "Stage Type";
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any(), any())).thenReturn(new StageData(caseUUID, stageType, ""));
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request, testUser);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateStageCreateException() throws EntityCreationException, EntityNotFoundException {
        final String stageType = "Stage Type";
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any(), any())).thenThrow(EntityCreationException.class);
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request, testUser);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStageFindException() throws EntityCreationException, EntityNotFoundException {
        final String stageType = "Stage Type";
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any(), any())).thenThrow(EntityNotFoundException.class);
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request, testUser);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data, testUser);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
