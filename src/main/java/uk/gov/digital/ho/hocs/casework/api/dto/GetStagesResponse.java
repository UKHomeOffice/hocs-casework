package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStagesResponse {

    @JsonProperty("stages")
    private Collection<GetStageResponse> stages;

    public static GetStagesResponse from(Collection<StageWithCaseData> stages) {
        Collection<GetStageResponse> stageDataResponses = stages.stream().map(GetStageResponse::from).collect(
            Collectors.toList());

        return new GetStagesResponse(stageDataResponses);
    }

}
