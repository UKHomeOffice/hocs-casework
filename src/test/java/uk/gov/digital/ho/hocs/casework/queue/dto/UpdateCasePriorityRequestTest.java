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
public class UpdateCasePriorityRequestTest {

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

    private String commandName = "update_case_priority_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCreateCaseNoteRequest() {

        UUID caseUUID = UUID.randomUUID();
        boolean priority = false;

        UpdateCasePriorityRequest updateCasePriorityRequest = new UpdateCasePriorityRequest(caseUUID, priority);

        assertThat(updateCasePriorityRequest.getCommand()).isEqualTo(commandName);
        assertThat(updateCasePriorityRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(updateCasePriorityRequest.isPriority()).isEqualTo(false);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        boolean priority = false;

        UpdateCasePriorityRequest updateCasePriorityRequest = new UpdateCasePriorityRequest(caseUUID, priority);

        updateCasePriorityRequest.execute(hocsCaseContext);

        verify(caseDataService, times(1)).updatePriority(caseUUID, priority);

        verifyNoMoreInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyZeroInteractions(correspondentService);
        verifyZeroInteractions(stageService);
        verifyZeroInteractions(topicService);

    }

}