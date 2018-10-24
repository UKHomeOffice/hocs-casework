package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.TopicService;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicDataRepository;

import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicServiceTest {

    @Mock
    private TopicDataRepository topicDataRepository;

    private TopicService topicService;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID TOPIC_UUID = UUID.randomUUID();
    private final String TOPIC_NAME = "TOPIC";

    @Before
    public void setUp() {
        this.topicService = new TopicService(
                topicDataRepository);
    }

    @Test
    public void shouldAddTopicToCase()  {

        when(topicDataRepository.findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID)).thenReturn(null);

        topicService.addTopicToCase(CASE_UUID, TOPIC_UUID, TOPIC_NAME);

        verify(topicDataRepository, times(1)).findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID);
        verify(topicDataRepository, times(1)).save(any());

        verifyNoMoreInteractions(topicDataRepository);
    }

    @Test
    public void shouldDeleteTopicFromCase() {

        Topic topic = new Topic(CASE_UUID, "Topic1", TOPIC_UUID);

        when(topicDataRepository.findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID)).thenReturn(topic);

        topicService.deleteTopicFromCase(CASE_UUID, TOPIC_UUID);

        verify(topicDataRepository, times(1)).findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID);
        verify(topicDataRepository, times(1)).save(any());

        verifyNoMoreInteractions(topicDataRepository);
    }
}

