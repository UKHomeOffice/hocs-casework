package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.StageResource;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.StageDto;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateStageRequest;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageResourceTest {

    @Mock
    private StageService stageService;

    private StageResource stageResource;

    @Before
    public void setUp() {
        stageResource = new StageResource(stageService);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null);

        when(stageService.createStage(uuid, stageType, teamUUID, null)).thenReturn(new Stage(uuid, stageType, uuid, uuid));

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(uuid, request);

        verify(stageService, times(1)).createStage(uuid, stageType, teamUUID, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldAllocateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        UpdateStageRequest request = new UpdateStageRequest(teamUUID, null);

        doNothing().when(stageService).allocateStage(uuid, uuid, teamUUID, null);

        ResponseEntity response = stageResource.allocateStage(uuid, uuid, request);

        verify(stageService, times(1)).allocateStage(uuid, uuid, teamUUID, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        when(stageService.getStage(uuid, uuid)).thenReturn(new Stage(uuid, stageType, uuid, uuid));

        ResponseEntity<StageDto> response = stageResource.getStage(uuid, uuid);

        verify(stageService, times(1)).getStage(uuid, uuid);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}