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

import java.io.UnsupportedEncodingException;
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
    private final UUID transitionNoteUUID = UUID.randomUUID();
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

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, allocationType, transitionNoteUUID, userUUID);

        when(stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService).createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageNoTransitionNote() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, null);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, allocationType, null, userUUID);

        when(stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, null)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService).createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldRecreateStage(){
        RecreateStageRequest request = new RecreateStageRequest(stageUUID, stageType);

        ResponseEntity response = stageResource.recreateStageTeam(caseUUID, stageUUID, request);

        verify(stageService).recreateStage(caseUUID, stageUUID, stageType);
        verifyNoMoreInteractions(stageService);
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStage() {

        Stage stage = new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageService.getActiveStage(caseUUID, stageUUID)).thenReturn(stage);

        ResponseEntity<GetStageResponse> response = stageResource.getStage(caseUUID, stageUUID);

        verify(stageService).getActiveStage(caseUUID, stageUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStage() {

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, userUUID);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(userUUID);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService).updateStageUser(caseUUID, stageUUID, userUUID);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStageNull() {

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, null);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(null);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService).updateStageUser(caseUUID, stageUUID, null);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStages() {

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStagesForUser()).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStages();

        verify(stageService).getActiveStagesForUser();

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesCaseRef() throws UnsupportedEncodingException {
        String ref = "MIN/0123456/19";

        Set<Stage> stages = new HashSet<>();

        when(stageService.getActiveStagesByCaseReference(ref)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesForCase(ref);

        verify(stageService).getActiveStagesByCaseReference(ref);

        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStageCaseUUIDsForUserAndTeam(){

        UUID userUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        ResponseEntity<Set<UUID>> response = stageResource.getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);

        verify(stageService).getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);
        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldSearch() {

        Set<Stage> stages = new HashSet<>();
        SearchRequest searchRequest = new SearchRequest();

        when(stageService.search(searchRequest)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.search(searchRequest);

        verify(stageService).search(searchRequest);
        verifyNoMoreInteractions(stageService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStageTypeFromStageData(){

        UUID userUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        ResponseEntity<String> response = stageResource.getStageTypeFromStageData(userUUID, teamUUID);

        verify(stageService).getStageTypeFromStageData(userUUID, teamUUID);
        verifyNoMoreInteractions(stageService);

        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void withdrawCase(){
        WithdrawCaseRequest withdrawCaseRequest = new WithdrawCaseRequest("Note 1", "2019-02-23");

        stageResource.withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);

        verify(stageService).withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);
        verifyNoMoreInteractions(stageService);
    }

}
