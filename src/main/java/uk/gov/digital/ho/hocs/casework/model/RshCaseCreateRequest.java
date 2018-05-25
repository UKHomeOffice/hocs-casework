package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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
}
