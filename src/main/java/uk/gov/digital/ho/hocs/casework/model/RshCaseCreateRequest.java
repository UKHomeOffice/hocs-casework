package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RshCaseCreateRequest {

    @JsonProperty("notify-email")
    private String notifyEmail;

    @JsonProperty("notify-team")
    private String notifyTeamName;

    @JsonProperty("caseData")
    private Map<String,Object> caseData;

    public String toJsonString(ObjectMapper objectMapper){
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
