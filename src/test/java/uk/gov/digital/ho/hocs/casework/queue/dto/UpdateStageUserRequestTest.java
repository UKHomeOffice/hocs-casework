package uk.gov.digital.ho.hocs.casework.queue.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.*;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStageUserRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    CorrespondentService correspondentService;

    @Mock
    StageService stageService;

    @Mock
    TopicService topicService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "update_stage_user_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCompleteStageRequest() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(caseUUID, stageUUID, userUUID);

        assertThat(updateStageUserRequest.getCommand()).isEqualTo(commandName);
        assertThat(updateStageUserRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(updateStageUserRequest.getStageUUID()).isEqualTo(stageUUID);
        assertThat(updateStageUserRequest.getUserUUID()).isEqualTo(userUUID);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID userUUID = UUID.randomUUID();

        UpdateStageUserRequest updateStageUserRequest = new UpdateStageUserRequest(caseUUID, stageUUID, userUUID);

        updateStageUserRequest.execute(hocsCaseContext);

        verify(stageService, times(1)).updateUser(caseUUID, stageUUID, userUUID);

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyZeroInteractions(correspondentService);
        verifyNoMoreInteractions(stageService);
        verifyZeroInteractions(topicService);

    }
}