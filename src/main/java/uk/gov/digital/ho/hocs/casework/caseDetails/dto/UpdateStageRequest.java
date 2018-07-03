package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;

import java.util.Map;

@AllArgsConstructor
@Getter
public class UpdateStageRequest {

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("stageData")
    private Map<String, String> stageData;
}
