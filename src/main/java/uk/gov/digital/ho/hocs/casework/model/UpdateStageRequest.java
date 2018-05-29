package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    @JsonProperty("stageUUID")
    private UUID stageUUID;

    @JsonProperty("schemaVersion")
    private int schemaVersion;

    @JsonProperty("stageData")
    private Map<String,Object> stageData;

    public String toJsonString(ObjectMapper objectMapper){
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
