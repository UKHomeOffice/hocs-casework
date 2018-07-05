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
import uk.gov.digital.ho.hocs.casework.caseDetails.model.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CaseDataResourceTest {

    @Mock
    private CaseDataService caseDataService;

    @Mock
    private DocumentService documentService;


    private CaseDataResource caseDataResource;

    private final UUID uuid = UUID.randomUUID();
    private final Map<String, String> data = new HashMap<>();
    private final String testUser = "Test User";
    private final StageType stageType = StageType.DCU_MIN_CATEGORISE;

    @Before
    public void setUp() {
        caseDataResource = new CaseDataResource(caseDataService, documentService);
    }

    @Test
    public void shouldCreateCaseWithNoDocuments() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;
        final List<DocumentSummary> documents = Collections.emptyList();

        when(caseDataService.createCase(any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType, documents);
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
    public void shouldCreateCaseWithADocument() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;
        final List<DocumentSummary> documents = Arrays.asList(
                new DocumentSummary(UUID.randomUUID(), "document.docx", DocumentType.ORIGINAL)
        );

        when(caseDataService.createCase(any(), any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType, documents);
        ResponseEntity<CreateCaseResponse> response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(uuid, caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUuid()).isNotNull();
        assertThat(response.getBody().getCaseReference()).isNotNull();
        assertThat(response.getBody()).isInstanceOf(CreateCaseResponse.class);
    }

    @Test
    public void shouldCreateCaseWithSeveralDocuments() throws EntityCreationException {
        final CaseType caseType = CaseType.MIN;
        final List<DocumentSummary> documents = Arrays.asList(
                new DocumentSummary(UUID.randomUUID(), "document.docx", DocumentType.ORIGINAL),
                new DocumentSummary(UUID.randomUUID(), "document (reloaded).doc", DocumentType.ORIGINAL),
                new DocumentSummary(UUID.randomUUID(), "document (revolutions).txt", DocumentType.ORIGINAL)
        );

        when(caseDataService.createCase(any())).thenReturn(new CaseData(caseType.toString(), 1234L));
        CreateCaseRequest request = new CreateCaseRequest(caseType, documents);
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
        final List<DocumentSummary> documents = Collections.emptyList();

         when(caseDataService.createCase(any(), any())).thenThrow(EntityCreationException.class);
        CreateCaseRequest request = new CreateCaseRequest(uuid, caseType, documents);
        ResponseEntity response = caseDataResource.createCase(request);

        verify(caseDataService, times(1)).createCase(uuid, caseType);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateCase() throws EntityCreationException, EntityNotFoundException {
        final CaseType caseType = CaseType.MIN;

        UUID caseUUID = UUID.randomUUID();

        when(caseDataService.updateCase(any(), any())).thenReturn(new CaseData(UUID.randomUUID(), caseType.toString(), 1234L));
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

        doNothing().when(caseDataService).createStage(any(UUID.class), any(UUID.class), anyString(), anyMap());
        CreateStageRequest request = new CreateStageRequest(uuid, StageType.DCU_MIN_CATEGORISE, data);

        ResponseEntity response = caseDataResource.createStage(uuid, request);

        verify(caseDataService, times(1)).createStage(uuid, uuid StageType.DCU_MIN_CATEGORISE, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageException() throws EntityCreationException {
        Map<String, String> data = new HashMap<>();

        doThrow(EntityCreationException.class).when(caseDataService).createStage(any(UUID.class), any(UUID.class), anyString(), anyMap());
        CreateStageRequest request = new CreateStageRequest(uuid, stageType, data);

        ResponseEntity response = caseDataResource.createStage(uuid, request);

        verify(caseDataService, times(1)).createStage(uuid, uuid, stageType, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {
        UUID caseUUID = UUID.randomUUID();
        Map<String, String> data = new HashMap<>();

        when(caseDataService.updateStage(any(), any(), any(), any())).thenReturn(new StageData(caseUUID, UUID.randomUUID(), stageType.toString(), ""));
      
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
