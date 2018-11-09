package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.*;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.model.StageStatusType;
import uk.gov.digital.ho.hocs.casework.domain.model.StageType;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageResourceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final LocalDate deadline = LocalDate.now();
    private final StageType stageType = StageType.DCU_MIN_MARKUP;
    private final StageStatusType statusType = StageStatusType.CREATED;
    @Mock
    private StageService stageService;
    private StageResource stageResource;

    @Before
    public void setUp() {
        stageResource = new StageResource(stageService);
    }

    @Test
    public void shouldCreateStage() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, null, deadline);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null, deadline);

        when(stageService.createStage(caseUUID, stageType, teamUUID, null, deadline)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, null, deadline);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageNoDeadline() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, null, null);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null, null);

        when(stageService.createStage(caseUUID, stageType, teamUUID, null, null)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, null, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStage() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, null, deadline);

        when(stageService.getStage(caseUUID, stageUUID)).thenReturn(stage);

        ResponseEntity<StageDto> response = stageResource.getStage(caseUUID, stageUUID);

        verify(stageService, times(1)).getStage(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldAllocateStageWithValidParams() {

        UpdateStageRequest request = new UpdateStageRequest(teamUUID, null, statusType);

        doNothing().when(stageService).updateStage(caseUUID, stageUUID, teamUUID, null, statusType);

        ResponseEntity response = stageResource.updateStage(caseUUID, stageUUID, request);

        verify(stageService, times(1)).updateStage(caseUUID, stageUUID, teamUUID, null, statusType);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesByUserUUID() {

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStagesByUserUUID(userUUID)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesByUserUUID(userUUID);

        verify(stageService, times(1)).getActiveStagesByUserUUID(userUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesByTeamUUID() {

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStagesByTeamUUID(teamUUID)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesByTeamUUID(teamUUID);

        verify(stageService, times(1)).getActiveStagesByTeamUUID(teamUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStages() {

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStages()).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStages();

        verify(stageService, times(1)).getActiveStages();

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}