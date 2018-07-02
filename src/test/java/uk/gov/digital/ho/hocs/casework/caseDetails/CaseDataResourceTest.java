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
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;

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
    private final StageType stageType = StageType.DCU_MIN_CATEGORISE;

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService);
    }

    @Test
    public void shouldCreateCase() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.createCase(any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody().getCaseReference()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateCaseResponse.class);
    }

    @Test
    public void shouldCreateCaseException() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.createCase(any())).thenThrow(EntityCreationException.class);
        CreateCaseRequest request = new CreateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        final CaseType caseType = CaseType.MIN;

        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.updateCase(any(), any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(caseUUID, request);

        verify(caseDataService, times(1)).updateCase(caseUUID, caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateCaseCreateException() throws EntityCreationException, EntityNotFoundException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.updateCase(any(), any())).thenThrow(EntityCreationException.class);
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(uuid, request);

        verify(caseDataService, times(1)).updateCase(uuid, caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCaseFindException() throws EntityCreationException, EntityNotFoundException {
        final CaseType caseType = CaseType.MIN;

        when(caseDataService.updateCase(any(), any())).thenThrow(EntityNotFoundException.class);
        UpdateCaseRequest request = new UpdateCaseRequest(caseType);
        ResponseEntity response = caseDataResource.updateCase(uuid, request);

        verify(caseDataService, times(1)).updateCase(uuid, caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldCreateStage() throws EntityCreationException {

        Map<String, String> data = new HashMap<>();

        when(caseDataService.createStage(any(UUID.class), any(StageType.class), anyMap())).thenReturn(new StageData(uuid, stageType.toString(), ""));
        CreateStageRequest request = new CreateStageRequest(StageType.DCU_MIN_CATEGORISE, data);

        ResponseEntity<CreateStageResponse> response = caseDataResource.createStage(uuid, request);

        verify(caseDataService, times(1)).createStage(uuid, StageType.DCU_MIN_CATEGORISE, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateStageResponse.class);
    }

    @Test
    public void shouldCreateStageException() throws EntityCreationException {
        Map<String, String> data = new HashMap<>();

        when(caseDataService.createStage(any(UUID.class), any(StageType.class), anyMap())).thenThrow(EntityCreationException.class);
        CreateStageRequest request = new CreateStageRequest(stageType, data);

        ResponseEntity response = caseDataResource.createStage(uuid, request);

        verify(caseDataService, times(1)).createStage(uuid, stageType, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any())).thenReturn(new StageData(caseUUID, stageType.toString(), ""));
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateStageCreateException() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any())).thenThrow(EntityCreationException.class);
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStageFindException() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any())).thenThrow(EntityNotFoundException.class);
        UpdateStageRequest request = new UpdateStageRequest(stageType, data);
        ResponseEntity response = caseDataResource.updateStage(caseUUID, uuid, request);

        verify(caseDataService, times(1)).updateStage(caseUUID, uuid, stageType, data);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
