package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TopicDto {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private UUID value;

    public static TopicDto from(Topic topic) {
        return new TopicDto(topic.getTopicName(), topic.getTopicNameUUID());
    }
}