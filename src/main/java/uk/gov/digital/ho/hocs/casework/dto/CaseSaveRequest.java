package uk.gov.digital.ho.hocs.casework.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CaseSaveRequest {
    @JsonProperty("notify-real-name")
    private String notifyRealName;

    @JsonProperty("notify-email")
    private String notifyEmail;

    @JsonProperty("notify-team-name")
    private String notifyTeamName;

    @JsonProperty("caseData")
    private String caseData;
}
