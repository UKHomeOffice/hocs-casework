package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStagesResponse {

    @JsonProperty("stages")
    private Set<GetStageResponse> stages;

    public static GetStagesResponse from(Set<Stage> stages) {
        Set<GetStageResponse> stageDataResponses = stages
                .stream()
                .map(GetStageResponse::from)
                .collect(Collectors.toSet());

        return new GetStagesResponse(stageDataResponses);
    }
}