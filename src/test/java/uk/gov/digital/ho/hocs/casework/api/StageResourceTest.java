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
    private final String allocationType = "anyAllocation";
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
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, deadline, allocationType);

        when(stageService.createStage(caseUUID, stageType, teamUUID, deadline, allocationType)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, deadline, allocationType);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageNoDeadline() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, null);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, null, allocationType);

        when(stageService.createStage(caseUUID, stageType, teamUUID, null, allocationType)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService, times(1)).createStage(caseUUID, stageType, teamUUID, null, allocationType);

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

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, userUUID);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(userUUID);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService, times(1)).updateStageUser(caseUUID, stageUUID, userUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStageNull() {

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, null);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(null);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService, times(1)).updateStageUser(caseUUID, stageUUID, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStages() {

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStagesForUser()).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStages();

        verify(stageService, times(1)).getActiveStagesForUser();

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}