package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AllocateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageResourceTest {

    @Mock
    private StageDataService stageDataService;

    private StageDataResource stageDataResource;

    @Before
    public void setUp() {
        stageDataResource = new StageDataResource(stageDataService);
    }

    @Test
    public void shouldCreateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null);

        when(stageDataService.createStage(uuid, stageType, teamUUID, null)).thenReturn(new Stage(uuid, stageType, uuid, uuid));

        ResponseEntity<CreateStageResponse> response = stageDataResource.createStage(uuid, request);

        verify(stageDataService, times(1)).createStage(uuid, stageType, teamUUID, null);

        verifyNoMoreInteractions(stageDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldAllocateStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();
        AllocateStageRequest request = new AllocateStageRequest(teamUUID, null);

        doNothing().when(stageDataService).allocateStage(uuid, teamUUID, null);

        ResponseEntity response = stageDataResource.allocateStage(uuid, uuid, request);

        verify(stageDataService, times(1)).allocateStage(uuid, teamUUID, null);

        verifyNoMoreInteractions(stageDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStageWithValidParams() {

        UUID uuid = UUID.randomUUID();
        StageType stageType = StageType.DCU_MIN_MARKUP;

        when(stageDataService.getStage(uuid)).thenReturn(new Stage(uuid, stageType, uuid, uuid));

        ResponseEntity<GetStageResponse> response = stageDataResource.getStage(uuid, uuid);

        verify(stageDataService, times(1)).getStage(uuid);

        verifyNoMoreInteractions(stageDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}