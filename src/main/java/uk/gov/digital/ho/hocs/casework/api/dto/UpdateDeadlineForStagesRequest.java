package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;


@AllArgsConstructor
@Getter
public class UpdateDeadlineForStagesRequest {

    @JsonProperty("stageTypeAndDaysMap")
    private Map<String, Integer> stageTypeAndDaysMap;
}
