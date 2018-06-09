package uk.gov.digital.ho.hocs.casework.search;

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
class SearchRequest {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseData")
    private Map<String, Object> caseData;

    public static String toJsonString(ObjectMapper objectMapper, SearchRequest searchRequest){
        try {
            return objectMapper.writeValueAsString(searchRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
