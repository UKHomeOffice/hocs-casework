package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateTopicRequest;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetTopicsResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicResourceTest {

    private final UUID caseUUID = UUID.randomUUID();

    private final UUID stageUUID = UUID.randomUUID();

    private final UUID topicUUID = UUID.randomUUID();

    private static final String topicName = "topicName";

    @Mock
    private TopicService topicService;

    private TopicResource topicResource;

    @Before
    public void setUp() {
        topicResource = new TopicResource(topicService);
    }

    @Test
    public void shouldAddTopicToCase() {

        when(topicService.createTopic(caseUUID, topicUUID)).thenReturn(new Topic(caseUUID, topicName, topicUUID));

        CreateTopicRequest createTopicRequest = new CreateTopicRequest(topicUUID);
        ResponseEntity<Void> response = topicResource.addTopicToCase(caseUUID, stageUUID, createTopicRequest);

        verify(topicService, times(1)).createTopic(caseUUID, topicUUID);

        verifyNoMoreInteractions(topicService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetTopics() {

        when(topicService.getTopics(caseUUID)).thenReturn(new HashSet<>());

        ResponseEntity<GetTopicsResponse> response = topicResource.getCaseTopics(caseUUID);

        verify(topicService, times(1)).getTopics(caseUUID);

        verifyNoMoreInteractions(topicService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetTopic() {

        when(topicService.getTopic(caseUUID, topicUUID)).thenReturn(new Topic(caseUUID, topicName, topicUUID));

        ResponseEntity<GetTopicResponse> response = topicResource.getTopic(caseUUID, topicUUID);

        verify(topicService, times(1)).getTopic(caseUUID, topicUUID);

        verifyNoMoreInteractions(topicService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteTopicFromCase() {

        doNothing().when(topicService).deleteTopic(caseUUID, topicUUID);

        ResponseEntity<Void> response = topicResource.deleteTopic(caseUUID, stageUUID, topicUUID);

        verify(topicService, times(1)).deleteTopic(caseUUID, topicUUID);

        verifyNoMoreInteractions(topicService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldGetAllTopics() {

        when(topicService.getAllTopics()).thenReturn(new HashSet<>());

        ResponseEntity<GetTopicsResponse> response = topicResource.getAllCaseTopics();

        verify(topicService, times(1)).getAllTopics();

        verifyNoMoreInteractions(topicService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
