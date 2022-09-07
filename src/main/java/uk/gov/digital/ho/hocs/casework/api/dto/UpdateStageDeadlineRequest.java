package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdateStageDeadlineRequest {

    @JsonProperty("stageType")
    private String stageType;

    @JsonProperty("days")
    private Integer days;

}
