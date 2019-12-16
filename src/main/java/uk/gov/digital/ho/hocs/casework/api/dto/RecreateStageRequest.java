package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
public class RecreateStageRequest {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("stageType")
    private String stageType;

}
