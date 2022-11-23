package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetWorkstacksResponse {

    @JsonProperty("stages")
    private Set<GetWorkstackResponse> stages;

    public static GetWorkstacksResponse from(Set<ActiveStage> stages) {
        Set<GetWorkstackResponse> stageDataResponses = stages.stream().map(GetWorkstackResponse::from).collect(
            Collectors.toSet());

        return new GetWorkstacksResponse(stageDataResponses);
    }

}
