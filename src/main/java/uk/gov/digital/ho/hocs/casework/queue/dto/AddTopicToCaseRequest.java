package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class AddTopicToCaseRequest {

    @JsonProperty("topicUUID")
    private UUID topicUUID;

    @JsonProperty("topicName")
    private String topicName;

}