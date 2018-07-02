package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageType;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateStageRequest {

    @JsonProperty("stageType")
    private StageType stageType;

    @JsonProperty("stageData")
    private Map<String, String> stageData;
}
