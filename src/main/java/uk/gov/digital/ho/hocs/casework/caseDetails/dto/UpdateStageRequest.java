package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateStageRequest {

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("stageData")
    private Map<String, String> stageData;
}
