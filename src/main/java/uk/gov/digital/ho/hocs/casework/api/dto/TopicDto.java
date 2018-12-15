package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class TopicDto {

    @JsonProperty("value")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("label")
    private String topicText;

    @JsonProperty("topicUUID")
    private UUID topicUUID;

    public static TopicDto from(Topic topic) {
        return new TopicDto(
                topic.getUuid(),
                topic.getCreated(),
                topic.getCaseUUID(),
                topic.getText(),
                topic.getTextUUID());
    }
}