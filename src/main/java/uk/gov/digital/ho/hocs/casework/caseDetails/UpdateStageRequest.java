package uk.gov.digital.ho.hocs.casework.caseDetails;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
class UpdateStageRequest {

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("schemaVersion")
    private int schemaVersion;

    @JsonProperty("stageData")
    private Map<String,Object> stageData;
}
