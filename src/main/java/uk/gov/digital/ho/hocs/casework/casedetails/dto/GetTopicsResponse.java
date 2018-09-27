package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.TopicData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetTopicsResponse {

    @JsonProperty("topics")
    private Set<GetTopicResponse> topics;

    public static GetTopicsResponse from(Set<TopicData> topicData) {
        Set<GetTopicResponse> topicsResponses = topicData
                .stream()
                .map(GetTopicResponse::from)
                .collect(Collectors.toSet());

        return new GetTopicsResponse(topicsResponses);
    }
}