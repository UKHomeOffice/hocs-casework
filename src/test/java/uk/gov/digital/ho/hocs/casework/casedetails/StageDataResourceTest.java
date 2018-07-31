package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityCreationException;
import uk.gov.digital.ho.hocs.casework.casedetails.exception.EntityNotFoundException;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageDataResourceTest {

    private final UUID uuid = UUID.randomUUID();
    private final Map<String, String> data = new HashMap<>();
    private final StageType stageType = StageType.DCU_MIN_MARKUP;
    @Mock
    private StageDataService stageDataService;
    private StageDataResource stageDataResource;

    @Before
    public void setUp() {
        stageDataResource = new StageDataResource(stageDataService);
    }

    @Test
    public void shouldCreateStage() throws EntityCreationException {
        UUID caseUUID = UUID.randomUUID();

        when(stageDataService.createStage(any(UUID.class), any(), anyMap())).thenReturn(new StageData(caseUUID, StageType.DCU_MIN_MARKUP.toString(), ""));
        CreateStageRequest request = new CreateStageRequest(StageType.DCU_MIN_MARKUP, data);

        ResponseEntity response = stageDataResource.createStage(uuid, request);

        verify(stageDataService, times(1)).createStage(uuid, StageType.DCU_MIN_MARKUP, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageException() throws EntityCreationException {

        doThrow(EntityCreationException.class).when(stageDataService).createStage(any(UUID.class), any(), anyMap());
        CreateStageRequest request = new CreateStageRequest(stageType, data);

        ResponseEntity response = stageDataResource.createStage(uuid, request);

        verify(stageDataService, times(1)).createStage(uuid, stageType, new HashMap<>());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Ignore
    @Test
    public void shouldUpdateStage() throws EntityCreationException, EntityNotFoundException {

        doNothing().when(stageDataService).completeStage(any(), any());

        //UpdateStageRequest request = new UpdateStageRequest(data);
        ResponseEntity response = stageDataResource.completeStage(uuid, uuid);

        verify(stageDataService, times(1)).completeStage(uuid, uuid);
        //verify(stageDataService, times(0)).completeStage(any(), any());
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();
    }

    @Test
    public void shouldUpdateStageCreateException() throws EntityCreationException, EntityNotFoundException {

        doThrow(EntityCreationException.class).when(stageDataService).completeStage(any(), any());

        //UpdateStageRequest request = new UpdateStageRequest(data);
        ResponseEntity response = stageDataResource.completeStage(uuid, uuid);

        verify(stageDataService, times(1)).completeStage(uuid, uuid);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void shouldUpdateStageFindException() throws EntityCreationException, EntityNotFoundException {

        doThrow(EntityNotFoundException.class).when(stageDataService).completeStage(any(), any());

        //UpdateStageRequest request = new UpdateStageRequest(data);
        ResponseEntity response = stageDataResource.completeStage(uuid, uuid);

        verify(stageDataService, times(1)).completeStage(uuid, uuid);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
