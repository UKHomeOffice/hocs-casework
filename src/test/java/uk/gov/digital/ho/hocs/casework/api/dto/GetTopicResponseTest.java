package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTopicResponseTest {

    @Test
    public void getTopicDtoTest() {

        UUID caseUUID = UUID.randomUUID();
        String topicName = "topicName";
        UUID topicNameUUID = UUID.randomUUID();

        Topic topic = new Topic(caseUUID, topicName, topicNameUUID);

        GetTopicResponse getTopicResponse = GetTopicResponse.from(topic);

        assertThat(getTopicResponse.getUuid()).isEqualTo(topic.getUuid());
        assertThat(getTopicResponse.getCreated()).isEqualTo(topic.getCreated());
        assertThat(getTopicResponse.getCaseUUID()).isEqualTo(topic.getCaseUUID());
        assertThat(getTopicResponse.getTopicText()).isEqualTo(topic.getText());
        assertThat(getTopicResponse.getTopicUUID()).isEqualTo(topic.getTextUUID());

    }

}