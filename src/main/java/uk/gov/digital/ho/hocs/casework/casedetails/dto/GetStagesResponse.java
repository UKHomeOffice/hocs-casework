package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Stage;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStagesResponse {

    @JsonProperty("stages")
    private Set<StageDto> stages;

    public static GetStagesResponse from(Set<Stage> stages) {
        Set<StageDto> stageDataResponses = stages
                .stream()
                .map(StageDto::from)
                .collect(Collectors.toSet());

        return new GetStagesResponse(stageDataResponses);
    }
}