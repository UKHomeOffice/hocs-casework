package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoTopic;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;
import uk.gov.digital.ho.hocs.casework.domain.repository.TopicRepository;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicServiceTest {

    private final UUID caseUUID = UUID.randomUUID();
    private final UUID topicUUID = UUID.randomUUID();
    private static final String topicName = "topicName";
    private final UUID topicNameUUID = UUID.randomUUID();
    @Mock
    private TopicRepository topicRepository;
    @Mock
    private InfoClient infoClient;
    private TopicService topicService;
    @Mock
    private AuditClient auditClient;

    @Before
    public void setUp() {
        topicService = new TopicService(topicRepository, infoClient, auditClient);
    }

    @Captor
    ArgumentCaptor<Topic> topicCaptor;

    @Test
    public void shouldGetTopics() throws ApplicationExceptions.EntityNotFoundException {
        HashSet<Topic> topicData = new HashSet<>();
        topicData.add(new Topic(caseUUID, topicName, topicNameUUID));

        when(topicRepository.findAllByCaseUUID(caseUUID)).thenReturn(topicData);

        topicService.getTopics(caseUUID);

        verify(topicRepository, times(1)).findAllByCaseUUID(caseUUID);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test
    public void shouldNotGetTopicsMissingUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        topicService.getTopics(null);

        verify(topicRepository, times(1)).findAllByCaseUUID(null);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test
    public void shouldCreateTopic() throws ApplicationExceptions.EntityCreationException {

        when(infoClient.getTopic(topicNameUUID)).thenReturn(new InfoTopic(topicName, topicNameUUID));

        topicService.createTopic(caseUUID, topicNameUUID);

        verify(topicRepository, times(1)).save(any(Topic.class));
        verify(infoClient, times(1)).getTopic(topicNameUUID);

        verifyNoMoreInteractions(topicRepository);
        verifyNoMoreInteractions(infoClient);

    }

    @Test
    public void shouldAuditCreateTopic() throws ApplicationExceptions.EntityCreationException {
        InfoTopic topic = new InfoTopic(topicName, topicNameUUID);
        when(infoClient.getTopic(topicNameUUID)).thenReturn(topic);

        topicService.createTopic(caseUUID, topicNameUUID);

        verify(auditClient, times(1)).createTopicAudit(topicCaptor.capture());
        assertThat(topicCaptor.getValue().getTextUUID()).isEqualTo(topic.getValue());
        assertThat(topicCaptor.getValue().getText()).isEqualTo(topic.getLabel());
        assertThat(topicCaptor.getValue().getCaseUUID()).isEqualTo(caseUUID);

        verifyNoMoreInteractions(auditClient);


    }

    @Test(expected = ApplicationExceptions.EntityCreationException.class)
    public void shouldNotCreateTopicMissingTextException() throws ApplicationExceptions.EntityCreationException {
        topicService.createTopic(caseUUID, null);
    }

    @Test
    public void shouldNotCreateTopicMissingText() {

        try {
            topicService.createTopic(caseUUID, null);
        } catch (ApplicationExceptions.EntityCreationException e) {
            // Do nothing.
        }

        verifyZeroInteractions(topicRepository);
        verifyZeroInteractions(infoClient);

    }

    @Test
    public void shouldGetTopic() throws ApplicationExceptions.EntityNotFoundException {

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        when(topicRepository.findByUUID(topic.getCaseUUID(), topic.getUuid())).thenReturn(topic);

        topicService.getTopic(topic.getCaseUUID(), topic.getUuid());

        verify(topicRepository, times(1)).findByUUID(topic.getCaseUUID(), topic.getUuid());

        verifyNoMoreInteractions(topicRepository);

    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetTopicNotFoundException() {

        when(topicRepository.findByUUID(caseUUID, topicUUID)).thenReturn(null);

        topicService.getTopic(caseUUID, topicUUID);
    }

    @Test
    public void shouldNotGetTopiceNotFound() {

        when(topicRepository.findByUUID(caseUUID, topicUUID)).thenReturn(null);

        try {
            topicService.getTopic(caseUUID, topicUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(topicRepository, times(1)).findByUUID(caseUUID, topicUUID);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetTopicMissingCaseUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        topicService.getTopic(null, topicUUID);

    }

    @Test
    public void shouldNotGetTopicMissingCaseUUID() {

        try {
            topicService.getTopic(null, topicUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(topicRepository, times(1)).findByUUID(null, topicUUID);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotGetTopicMissingTopicUUIDException() throws ApplicationExceptions.EntityNotFoundException {
        topicService.getTopic(caseUUID, null);

    }

    @Test
    public void shouldDeleteTopicFromCase() {

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        when(topicRepository.findByUUID(topic.getCaseUUID(), topic.getUuid())).thenReturn(topic);

        topicService.deleteTopic(topic.getCaseUUID(), topic.getUuid());

        verify(topicRepository, times(1)).findByUUID(topic.getCaseUUID(), topic.getUuid());
        verify(topicRepository, times(1)).save(topic);


        verifyNoMoreInteractions(topicRepository);
    }

    @Test
    public void shouldAuditDeleteTopicFromCase() {

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        when(topicRepository.findByUUID(topic.getCaseUUID(), topic.getUuid())).thenReturn(topic);

        topicService.deleteTopic(topic.getCaseUUID(), topic.getUuid());

        verify(topicRepository, times(1)).findByUUID(topic.getCaseUUID(), topic.getUuid());
        verify(topicRepository, times(1)).save(topic);


        verifyNoMoreInteractions(topicRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteTopicMissingCaseUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        topicService.deleteTopic(null, topicUUID);

    }

    @Test
    public void shouldNotDeleteTopicMissingCaseUUID() {

        try {
            topicService.deleteTopic(null, topicUUID);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(topicRepository, times(1)).findByUUID(null, topicUUID);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test(expected = ApplicationExceptions.EntityNotFoundException.class)
    public void shouldNotDeleteTopicMissingTopicUUIDException() throws ApplicationExceptions.EntityNotFoundException {

        topicService.deleteTopic(caseUUID, null);

    }

    @Test
    public void shouldNotDeleteTopicMissingTopicUUID() {

        try {
            topicService.deleteTopic(caseUUID, null);
        } catch (ApplicationExceptions.EntityNotFoundException e) {
            // Do nothing.
        }

        verify(topicRepository, times(1)).findByUUID(caseUUID, null);

        verifyNoMoreInteractions(topicRepository);
    }

    @Test
    public void shouldEvictFromTheCache() {

        topicService.clearCachedStandardLineForTopic(topicUUID);

        verify(infoClient, times(1)).clearCachedStandardLineForTopic(topicUUID);
        verifyNoMoreInteractions(infoClient);
    }

    @Test
    public void shouldGetAllTopics() {
        HashSet<Topic> topicData = new HashSet<>();
        topicData.add(new Topic(caseUUID, topicName, topicNameUUID));

        when(topicRepository.findAllTopics()).thenReturn(topicData);

        topicService.getAllTopics();

        verify(topicRepository, times(1)).findAllTopics();

        verifyNoMoreInteractions(topicRepository);
    }
}
