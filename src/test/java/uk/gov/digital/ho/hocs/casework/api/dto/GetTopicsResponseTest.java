package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTopicsResponseTest {

    @Test
    public void getGetTopicsResponseResponse() {

        UUID caseUUID = UUID.randomUUID();
        String topicName = "topicName";
        UUID topicNameUUID = UUID.randomUUID();

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        Set<Topic> topics = new HashSet<>();
        topics.add(topic);

        GetTopicsResponse getTopicsResponse = GetTopicsResponse.from(topics);

        assertThat(getTopicsResponse.getTopics()).hasSize(1);

    }

    @Test
    public void getGetTopicsResponseEmpty() {

        Set<Topic> topics = new HashSet<>();

        GetTopicsResponse getTopicsResponse = GetTopicsResponse.from(topics);

        assertThat(getTopicsResponse.getTopics()).hasSize(0);

    }

}