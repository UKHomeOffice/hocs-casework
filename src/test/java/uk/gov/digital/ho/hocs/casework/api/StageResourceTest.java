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
    private final String stageType = "DCU_MIN_MARKUP";
    @Mock
    private StageService stageService;
    private StageResource stageResource;

    @Before
    public void setUp() {
        stageResource = new StageResource(stageService);
    }

    @Test
    public void shouldCreateStage() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, deadline);

        when(stageService.createStage(caseUUID, stageType, teamUUID, deadline)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, deadline);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageNoDeadline() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, null);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null);

        when(stageService.createStage(caseUUID, stageType, teamUUID, null)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStage() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, deadline);

        when(stageService.getStage(caseUUID, stageUUID)).thenReturn(stage);

        ResponseEntity<StageDto> response = stageResource.getStage(caseUUID, stageUUID);

        verify(stageService, times(1)).getStage(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStage() {

        doNothing().when(stageService).updateUser(caseUUID, stageUUID, userUUID);

        AllocateStageRequest allocateStageRequest = new AllocateStageRequest(userUUID);

        ResponseEntity response = stageResource.allocateStage(caseUUID, stageUUID, allocateStageRequest);

        verify(stageService, times(1)).updateUser(caseUUID, stageUUID, userUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStageNull() {

        doNothing().when(stageService).updateUser(caseUUID, stageUUID, null);

        AllocateStageRequest allocateStageRequest = new AllocateStageRequest(null);

        ResponseEntity response = stageResource.allocateStage(caseUUID, stageUUID, allocateStageRequest);

        verify(stageService, times(1)).updateUser(caseUUID, stageUUID, null);

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