package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetTopicResponse {

    @JsonProperty("label")
    private String label;

    @JsonProperty("value")
    private UUID value;

    public static GetTopicResponse from(TopicData topicData) {
        return new GetTopicResponse(topicData.getTopicName(), topicData.getTopicUUID());
    }
}