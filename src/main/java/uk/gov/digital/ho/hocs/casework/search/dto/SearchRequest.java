package uk.gov.digital.ho.hocs.casework.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchRequest {

    @JsonProperty("caseReference")
    private String caseReference = "";

    @JsonProperty("caseData")
    private Map<String, String> caseData = new HashMap<>();

    public static String toJsonString(ObjectMapper objectMapper, SearchRequest searchRequest){
        String ret = "";
        try {
            ret = objectMapper.writeValueAsString(searchRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
