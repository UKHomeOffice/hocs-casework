package uk.gov.digital.ho.hocs.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CaseSaveRequest {

    @JsonProperty("notify-email")
    private String notifyEmail;

    @JsonProperty("notify-team")
    private String notifyTeamName;

    @JsonProperty("caseData")
    private Map<String,String> caseData;
}
