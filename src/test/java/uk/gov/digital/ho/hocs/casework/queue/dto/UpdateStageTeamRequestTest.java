package uk.gov.digital.ho.hocs.casework.queue.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStageTeamRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    StageService stageService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "update_stage_team_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, stageService);
    }

    @Test
    public void getCompleteStageRequest() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        UpdateStageTeamRequest updateStageTeamRequest = new UpdateStageTeamRequest(caseUUID, stageUUID, teamUUID);

        assertThat(updateStageTeamRequest.getCommand()).isEqualTo(commandName);
        assertThat(updateStageTeamRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(updateStageTeamRequest.getStageUUID()).isEqualTo(stageUUID);
        assertThat(updateStageTeamRequest.getTeamUUID()).isEqualTo(teamUUID);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID teamUUID = UUID.randomUUID();

        UpdateStageTeamRequest updateStageTeamRequest = new UpdateStageTeamRequest(caseUUID, stageUUID, teamUUID);

        updateStageTeamRequest.execute(hocsCaseContext);

        verify(stageService, times(1)).updateTeam(caseUUID, stageUUID, teamUUID);

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyNoMoreInteractions(stageService);

    }
}