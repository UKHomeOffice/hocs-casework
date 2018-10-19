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
public class GetActiveStagesResponse {

    @JsonProperty("activeStages")
    private Set<ActiveStageDto> activeStageDtos;

    public static GetActiveStagesResponse from(Set<Stage> activeStages) {
        Set<ActiveStageDto> stageResponses = activeStages
                .stream()
                .map(ActiveStageDto::from)
                .collect(Collectors.toSet());

        return new GetActiveStagesResponse(stageResponses);
    }
}