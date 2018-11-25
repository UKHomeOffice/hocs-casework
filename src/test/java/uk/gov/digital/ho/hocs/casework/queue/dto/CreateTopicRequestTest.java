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
public class CreateTopicRequestTest {

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

    private String commandName = "create_topic_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCreateCaseNoteRequest() {

        UUID caseUUID = UUID.randomUUID();
        UUID topicNameUUID = UUID.randomUUID();
        String topicName = "anyName";

        CreateTopicRequest createTopicRequest = new CreateTopicRequest(caseUUID, topicName, topicNameUUID);

        assertThat(createTopicRequest.getCommand()).isEqualTo(commandName);
        assertThat(createTopicRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(createTopicRequest.getTopicName()).isEqualTo(topicName);
        assertThat(createTopicRequest.getTopicNameUUID()).isEqualTo(topicNameUUID);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        UUID topicNameUUID = UUID.randomUUID();
        String topicName = "anyName";

        doNothing().when(topicService).createTopic(caseUUID, topicName, topicNameUUID);

        CreateTopicRequest createTopicRequest = new CreateTopicRequest(caseUUID, topicName, topicNameUUID);

        createTopicRequest.execute(hocsCaseContext);

        verify(topicService, times(1)).createTopic(caseUUID, topicName, topicNameUUID);

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyZeroInteractions(correspondentService);
        verifyZeroInteractions(stageService);
        verifyNoMoreInteractions(topicService);

    }

}