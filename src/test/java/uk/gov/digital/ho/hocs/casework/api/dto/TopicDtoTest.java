package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class TopicDtoTest {

    @Test
    public void getTopicDtoTest() {

        UUID caseUUID = UUID.randomUUID();
        String topicName = "topicName";
        UUID topicNameUUID = UUID.randomUUID();

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        TopicDto topicDto = TopicDto.from(topic);

        assertThat(topicDto.getUuid()).isEqualTo(topic.getUuid());
        assertThat(topicDto.getCreated()).isEqualTo(topic.getCreated());
        assertThat(topicDto.getCaseUUID()).isEqualTo(topic.getCaseUUID());
        assertThat(topicDto.getText()).isEqualTo(topic.getText());
        assertThat(topicDto.getTopicUUID()).isEqualTo(topic.getTopicUUID());

    }

}