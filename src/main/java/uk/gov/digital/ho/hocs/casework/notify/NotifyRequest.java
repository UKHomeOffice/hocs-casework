package uk.gov.digital.ho.hocs.casework.notify;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NotifyRequest {

    @JsonProperty("notifyEmail")
    private String notifyEmail;

    @JsonProperty("notifyTeam")
    private String notifyTeamName;
}
