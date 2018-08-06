package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetActiveStageResponse {

    @JsonProperty("activeStages")
    private Set<ActiveStageDto> activeStages;

    public static GetActiveStageResponse from(Set<ActiveStage> activeStages) {
        Set<ActiveStageDto> stageResponses = activeStages
                .stream()
                .map(ActiveStageDto::from)
                .collect(Collectors.toSet());

        return new GetActiveStageResponse(stageResponses);
    }
}