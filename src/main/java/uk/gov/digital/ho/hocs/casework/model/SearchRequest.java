package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SearchRequest {

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("caseData")
    private Map<String, String> caseData;
}
