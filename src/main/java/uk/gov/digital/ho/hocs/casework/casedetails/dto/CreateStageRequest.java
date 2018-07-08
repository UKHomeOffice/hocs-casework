package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateStageRequest {

    @JsonProperty("type")
    private StageType type;

    @JsonProperty("data")
    private Map<String, String> data;
}
