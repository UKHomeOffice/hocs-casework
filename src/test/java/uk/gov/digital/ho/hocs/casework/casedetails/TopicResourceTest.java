package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.TopicResource;
import uk.gov.digital.ho.hocs.casework.api.TopicService;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class TopicResourceTest {

    @Mock
    private TopicService topicService;

    private TopicResource topicResource;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID TOPIC_UUID = UUID.randomUUID();
    private final String TOPIC_NAME = "TOPIC";

    @Before
    public void setUp() {
        topicResource = new TopicResource(topicService);
    }

    @Test
    public void shouldAddTopicToCase() {

        //when(topicService.addTopicToCase(CASE_UUID, TOPIC_UUID, TOPIC_NAME)).thenReturn(new Topic(UUID.randomUUID(), "anyName", UUID.randomUUID()));

        //CreateTopicRequest request = new CreateTopicRequest(TOPIC_UUID, "TOPIC");

        //ResponseEntity response = topicResource.addTopicToCase(CASE_UUID, request);

        //verify(topicService, times(1)).addTopicToCase(CASE_UUID, TOPIC_UUID, TOPIC_NAME);

        verifyNoMoreInteractions(topicService);

        //assertThat(response).isNotNull();
        //assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void shouldDeleteTopicFromCase() {
        //when(topicService.deleteTopicFromCase(CASE_UUID, TOPIC_UUID)).thenReturn(new Topic(UUID.randomUUID(), "anyName", UUID.randomUUID()));

        //ResponseEntity response = topicResource.deleteTopicFromCase(CASE_UUID, TOPIC_UUID);

        //verify(topicService, times(1)).deleteTopicFromCase(CASE_UUID, TOPIC_UUID);

        verifyNoMoreInteractions(topicService);

        //assertThat(response).isNotNull();
        //assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
