package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.AddTopicToCaseRequest;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Topic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicResourceTest {

    @Mock
    private TopicDataService topicDataService;

    private TopicDataResource topicDataResource;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID TOPIC_UUID = UUID.randomUUID();
    private final String TOPIC_NAME = "TOPIC";

    @Before
    public void setUp() {
        topicDataResource = new TopicDataResource(topicDataService);
    }

    @Test
    public void shouldAddTopicToCase() {

        when(topicDataService.addTopicToCase(CASE_UUID, TOPIC_UUID, TOPIC_NAME)).thenReturn(new Topic());

        AddTopicToCaseRequest request = new AddTopicToCaseRequest(TOPIC_UUID,"TOPIC" );

        ResponseEntity response = topicDataResource.addTopicToCase(CASE_UUID, request);

        verify(topicDataService, times(1)).addTopicToCase(CASE_UUID,TOPIC_UUID, TOPIC_NAME);

        verifyNoMoreInteractions(topicDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteTopicFromCase() {
        when(topicDataService.deleteTopicFromCase(CASE_UUID, TOPIC_UUID)).thenReturn(new Topic());

        ResponseEntity response = topicDataResource.deleteTopicFromCase(CASE_UUID,TOPIC_UUID );

        verify(topicDataService, times(1)).deleteTopicFromCase(CASE_UUID,TOPIC_UUID );

        verifyNoMoreInteractions(topicDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
