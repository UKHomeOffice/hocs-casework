package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.GetStageResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetWorkstacksResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@WebMvcTest(WorkstackResource.class)
@RunWith(SpringRunner.class)
public class WorkstackResourceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID teamUUID = UUID.randomUUID();

    private final UUID userUUID = UUID.randomUUID();

    private final UUID transitionNoteUUID = UUID.randomUUID();

    private final String stageType = "DCU_MIN_MARKUP";

    @MockBean
    private WorkstackService workstackService;

    private WorkstackResource workstackResource;

    @Before
    public void setUp() {
        workstackResource = new WorkstackResource(workstackService);
    }

    @Test
    public void testShouldGetActiveStages() {

        Set<ActiveStage> stages = new HashSet<>();

        when(workstackService.getActiveStagesForUsersTeams()).thenReturn(stages);

        ResponseEntity<GetWorkstacksResponse> response = workstackResource.getActiveStages();

        verify(workstackService).getActiveStagesForUsersTeams();

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testShouldGetActiveStagesForUser() {
        Set<ActiveStage> stages = new HashSet<>();

        when(workstackService.getActiveUserStagesWithTeamsForUser(userUUID)).thenReturn(stages);

        ResponseEntity<GetWorkstacksResponse> response = workstackResource.getActiveStagesForUser(userUUID);

        verify(workstackService).getActiveUserStagesWithTeamsForUser(userUUID);

        checkNoMoreInteractions();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testAllocateStageUser() {
        CaseData caseData = new CaseData(caseUUID, null, "COMP", "COMP/123456/22", false, Map.of(), null, null, null,
            null, Collections.emptySet(), null, null, null, false, null, Collections.emptySet(), Set.of());

        ActiveStage stage = new ActiveStage(caseUUID, LocalDateTime.now(), stageType, null, null, transitionNoteUUID,
            caseUUID, teamUUID, userUUID, caseData, null, null, null);
        when(workstackService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID)).thenReturn(stage);

        ResponseEntity<GetStageResponse> response = workstackResource.allocateStageUser(teamUUID, userUUID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void testAllocateStageUser_withNoStage() {

        when(workstackService.getUnassignedAndActiveStageByTeamUUID(teamUUID, userUUID)).thenReturn(null);

        ResponseEntity<GetStageResponse> response = workstackResource.allocateStageUser(teamUUID, userUUID);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    private void checkNoMoreInteractions() {

        verifyNoMoreInteractions(workstackService);
    }

}
