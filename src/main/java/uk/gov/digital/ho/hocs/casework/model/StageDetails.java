package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StageDetails {

    @JsonProperty("stageName")
    private String stageName;

    @JsonProperty("caseData")
    private Map<String,String> caseData;

    public static StageDetails from(String stageName, Map<String,String> caseData){
        return new StageDetails(stageName, caseData);
    }
}
