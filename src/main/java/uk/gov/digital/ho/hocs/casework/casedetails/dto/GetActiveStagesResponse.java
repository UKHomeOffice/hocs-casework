package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetActiveStagesResponse {

    @JsonProperty("activeStages")
    private Set<ActiveStage> activeStages;

    public static GetActiveStagesResponse from(Set<uk.gov.digital.ho.hocs.casework.casedetails.model.ActiveStage> activeStages) {
        Set<ActiveStage> stageResponses = activeStages
                .stream()
                .map(ActiveStage::from)
                .collect(Collectors.toSet());

        return new GetActiveStagesResponse(stageResponses);
    }
}