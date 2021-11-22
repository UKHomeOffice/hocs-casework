package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateStageResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStagesResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.RecreateStageRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.SearchRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.UpdateStageUserRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.WithdrawCaseRequest;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(StageResource.class)
@RunWith(SpringRunner.class)
public class StageResourceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID stageUUID = UUID.randomUUID();
    private final UUID transitionNoteUUID = UUID.randomUUID();
    private final String stageType = "DCU_MIN_MARKUP";
    private final String allocationType = "anyAllocation";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StageService stageService;

    @MockBean
    private InfoClient infoClient;

    private StageResource stageResource;

    @Before
    public void setUp() {
        stageResource = new StageResource(stageService, infoClient);
    }

    @Test
    public void shouldCreateStage() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, allocationType, transitionNoteUUID, userUUID);

        when(stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService).createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, transitionNoteUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldCreateStageNoTransitionNote() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, null);
        CreateStageRequest request = new CreateStageRequest(stageType, teamUUID, allocationType, null, userUUID);

        when(stageService.createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, null)).thenReturn(stage);

        ResponseEntity<CreateStageResponse> response = stageResource.createStage(caseUUID, request);

        verify(stageService).createStage(caseUUID, stageType, teamUUID, userUUID, allocationType, null);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldRecreateStage() {
        RecreateStageRequest request = new RecreateStageRequest(stageUUID, stageType);

        ResponseEntity response = stageResource.recreateStageTeam(caseUUID, stageUUID, request);

        verify(stageService).recreateStage(caseUUID, stageUUID, stageType);
        checkNoMoreInteractions();
        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStage() {

        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);

        when(stageService.getActiveStage(caseUUID, stageUUID)).thenReturn(stage);

        ResponseEntity<GetStageResponse> response = stageResource.getStage(caseUUID, stageUUID);

        verify(stageService).getActiveStage(caseUUID, stageUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStage() {

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, userUUID);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(userUUID);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService).updateStageUser(caseUUID, stageUUID, userUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldAllocateStageNull() {

        doNothing().when(stageService).updateStageUser(caseUUID, stageUUID, null);

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(null);

        ResponseEntity response = stageResource.updateStageUser(caseUUID, stageUUID, updateStageUserRequest);

        verify(stageService).updateStageUser(caseUUID, stageUUID, null);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesCaseUUID() {
        Set<StageWithCaseData> stages = new HashSet<>();
        UUID caseUUID = UUID.randomUUID();

        when(stageService.getActiveStagesByCaseUUID(caseUUID)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesByCase(caseUUID);

        verify(stageService).getActiveStagesByCaseUUID(caseUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStages() {

        Set<StageWithCaseData> stages = new HashSet<>();

        when(stageService.getActiveStagesForUsersTeamsAndCaseType()).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStages();

        verify(stageService).getActiveStagesForUsersTeamsAndCaseType();

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesForUser() {
        Set<StageWithCaseData> stages = new HashSet<>();

        when(stageService.getActiveUserStagesWithTeamsAndCaseType(userUUID)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesForUser(userUUID);

        verify(stageService).getActiveUserStagesWithTeamsAndCaseType(userUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetActiveStagesCaseRef() throws UnsupportedEncodingException {
        String ref = "MIN/0123456/19";

        Set<StageWithCaseData> stages = new HashSet<>();

        when(stageService.getActiveStagesByCaseReference(ref)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.getActiveStagesForCase(ref);

        verify(stageService).getActiveStagesByCaseReference(ref);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    /**
     * Test to ensure the reference regex is used.
     * @throws Exception
     */
    @Test
    public void shouldUseReferenceRegex() throws Exception {

        // given
        // These are all BAD references
        String references[] = new String[] {
                "A/1234567/99", // caseType is < 2
                "AA/12345678/99", //seqNo.length > 7
                "AA/A234567/99", // seqNo contains A
                "AA/1234567/A9", // year suffix contains A
                "5a1562e2-052b-44fa-87be-376b7cee489b", // The wrong  identifier
                "A duff string" }; // duff

        // when
        for (String badRef : references) {
            mockMvc.perform(get("/case/{reference}/stage", URLEncoder.encode(badRef, StandardCharsets.UTF_8.name())))
                    // then
                    .andExpect(result -> assertThat(result.getResolvedException())
                            .isInstanceOf(HttpRequestMethodNotSupportedException.class)
                    );
        }

    }

    @Test
    public void shouldGetActiveStageCaseUUIDsForUserAndTeam() {

        UUID userUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        ResponseEntity<Set<UUID>> response = stageResource.getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);

        verify(stageService).getActiveStageCaseUUIDsForUserAndTeam(userUUID, teamUUID);
        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    }

    @Test
    public void shouldSearch() {

        Set<StageWithCaseData> stages = new HashSet<>();
        SearchRequest searchRequest = new SearchRequest();

        when(stageService.search(searchRequest)).thenReturn(stages);

        ResponseEntity<GetStagesResponse> response = stageResource.search(searchRequest);

        verify(stageService).search(searchRequest);
        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetStageTypeFromStageData() {

        UUID userUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        ResponseEntity<String> response = stageResource.getStageTypeFromStageData(userUUID, teamUUID);

        verify(stageService).getStageType(userUUID, teamUUID);
        checkNoMoreInteractions();

        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void withdrawCase() {
        WithdrawCaseRequest withdrawCaseRequest = new WithdrawCaseRequest("Note 1", "2019-02-23");

        stageResource.withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);

        verify(stageService).withdrawCase(caseUUID, stageUUID, withdrawCaseRequest);
        checkNoMoreInteractions();
    }

    @Test
    public void getUsersForTeamByStage_stageTeamFound() {

        List<UserDto> users = List.of(new UserDto(UUID.randomUUID().toString(), "username", "firstName", "lastName", "email@test.com"));
        UUID teamUUID = UUID.randomUUID();
        when(stageService.getStageTeam(caseUUID, stageUUID)).thenReturn(teamUUID);
        when(infoClient.getUsersForTeam(teamUUID)).thenReturn(users);

        ResponseEntity<List<UserDto>> response = stageResource.getUsersForTeamByStage(caseUUID, stageUUID);

        verify(stageService).getStageTeam(caseUUID, stageUUID);
        verify(infoClient).getUsersForTeam(teamUUID);
        checkNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(users);

    }

    @Test
    public void getUsersForTeamByStage_stageTeamNotFound() {

        List<UserDto> users = List.of(new UserDto(UUID.randomUUID().toString(), "username", "firstName", "lastName", "email@test.com"));
        when(stageService.getStageTeam(caseUUID, stageUUID)).thenReturn(null);
        when(infoClient.getUsersForTeamByStage(caseUUID, stageUUID)).thenReturn(users);

        ResponseEntity<List<UserDto>> response = stageResource.getUsersForTeamByStage(caseUUID, stageUUID);

        verify(stageService).getStageTeam(caseUUID, stageUUID);
        verify(infoClient).getUsersForTeamByStage(caseUUID, stageUUID);
        checkNoMoreInteractions();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(users);

    }

    @Test
    public void allocateStageUser() {
        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        when(stageService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID)).thenReturn(stage);

        ResponseEntity<GetStageResponse> response = stageResource.allocateStageUser(teamUUID, userUUID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void allocateStageUser_withNoStage() {
        StageWithCaseData stage = new StageWithCaseData(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID);
        when(stageService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID)).thenReturn(null);

        ResponseEntity<GetStageResponse> response = stageResource.allocateStageUser(teamUUID, userUUID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void checkNoMoreInteractions() {

        verifyNoMoreInteractions(stageService, infoClient);
    }

}
