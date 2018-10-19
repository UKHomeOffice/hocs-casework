package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Topic;
import uk.gov.digital.ho.hocs.casework.casedetails.repository.TopicDataRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicServiceTest {

    @Mock
    private TopicDataRepository topicDataRepository;

    private TopicDataService topicDataService;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID TOPIC_UUID = UUID.randomUUID();
    private final String TOPIC_NAME = "TOPIC";

    @Before
    public void setUp() {
        this.topicDataService = new TopicDataService(
                topicDataRepository);
    }

    @Test
    public void shouldAddTopicToCase()  {

        when(topicDataRepository.findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID)).thenReturn(null);

        topicDataService.addTopicToCase(CASE_UUID,TOPIC_UUID, TOPIC_NAME);

        verify(topicDataRepository, times(1)).findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID);
        verify(topicDataRepository, times(1)).save(any());

        verifyNoMoreInteractions(topicDataRepository);
    }

    @Test
    public void shouldDeleteTopicFromCase() {

        Topic topic = new Topic(1, CASE_UUID, "Topic1", TOPIC_UUID, LocalDate.now(), null, Boolean.FALSE);

        when(topicDataRepository.findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID)).thenReturn(topic);

        topicDataService.deleteTopicFromCase(CASE_UUID,TOPIC_UUID);

        verify(topicDataRepository, times(1)).findByCaseUUIDAndTopicUUID(CASE_UUID, TOPIC_UUID);
        verify(topicDataRepository, times(1)).save(any());

        verifyNoMoreInteractions(topicDataRepository);
    }
}

