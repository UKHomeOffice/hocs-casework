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
public class CompleteStageRequestTest {

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

    private String commandName = "complete_stage_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCompleteStageRequest() {

        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        CompleteStageRequest completeStageRequest = new CompleteStageRequest(caseUUID, stageUUID);

        assertThat(completeStageRequest.getCommand()).isEqualTo(commandName);
        assertThat(completeStageRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(completeStageRequest.getStageUUID()).isEqualTo(stageUUID);

    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();

        doNothing().when(stageService).completeStage(caseUUID, stageUUID);

        CompleteStageRequest completeStageRequest = new CompleteStageRequest(caseUUID, stageUUID);

        completeStageRequest.execute(hocsCaseContext);

        verify(stageService, times(1)).completeStage(caseUUID, stageUUID);

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyZeroInteractions(correspondentService);
        verifyNoMoreInteractions(stageService);
        verifyZeroInteractions(topicService);

    }
}