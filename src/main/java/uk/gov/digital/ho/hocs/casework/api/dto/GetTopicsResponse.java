package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetTopicsResponse {

    @JsonProperty("topics")
    private Set<GetTopicResponse> topics;

    public static GetTopicsResponse from(Set<Topic> topicData) {
        Set<GetTopicResponse> topicsResponses = topicData.stream().map(GetTopicResponse::from).collect(
            Collectors.toSet());

        return new GetTopicsResponse(topicsResponses);
    }

}
