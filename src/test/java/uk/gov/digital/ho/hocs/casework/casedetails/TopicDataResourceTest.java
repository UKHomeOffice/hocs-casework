package uk.gov.digital.ho.hocs.casework.casedetails;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.digital.ho.hocs.casework.casedetails.dto.*;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TopicDataResourceTest {

    @Mock
    private TopicDataService topicDataService;

    private TopicDataResource topicDataResource;

    private final UUID CASE_UUID = UUID.randomUUID();
    private final UUID TOPIC_UUID = UUID.randomUUID();

    @Before
    public void setUp() {
        topicDataResource = new TopicDataResource(topicDataService);
    }

    @Test
    public void shouldAddTopicToCase() {

        when(topicDataService.addTopicToCase(CASE_UUID,TOPIC_UUID)).thenReturn(new TopicData());

        AddTopicToCaseRequest request = new AddTopicToCaseRequest(TOPIC_UUID,"TOPIC" );

        ResponseEntity response = topicDataResource.addTopicToCase(CASE_UUID, request);

        verify(topicDataService, times(1)).addTopicToCase(CASE_UUID,TOPIC_UUID );

        verifyNoMoreInteractions(topicDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


    @Test
    public void shouldDeleteTopicFromCase() {
        when(topicDataService.deleteTopicFromCase(CASE_UUID,TOPIC_UUID)).thenReturn(new TopicData());

        ResponseEntity response = topicDataResource.deleteTopicFromCase(CASE_UUID,TOPIC_UUID );

        verify(topicDataService, times(1)).deleteTopicFromCase(CASE_UUID,TOPIC_UUID );

        verifyNoMoreInteractions(topicDataService);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }


}
