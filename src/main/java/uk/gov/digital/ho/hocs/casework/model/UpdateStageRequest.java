package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.beans.binding.ObjectExpression;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class UpdateStageRequest {

    @JsonProperty("requestUUID")
    private String requestUUID;

    @JsonProperty("requestTimestamp")
    private String requestTimestamp;

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("schemaVersion")
    private int schemaVersion;

    @JsonProperty("stageData")
    private Map<String,Object> stageData;
}
