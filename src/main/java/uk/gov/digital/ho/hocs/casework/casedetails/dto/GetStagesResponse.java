package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStagesResponse {

    @JsonProperty("stages")
    private Set<GetStageResponse> stages;

    public static GetStagesResponse from(Set<StageData> stageDatas) {
        Set<GetStageResponse> stageDataResponses = stageDatas
                .stream()
                .map(GetStageResponse::from)
                .collect(Collectors.toSet());

        return new GetStagesResponse(stageDataResponses);
    }
}