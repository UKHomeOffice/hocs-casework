package uk.gov.digital.ho.hocs.casework.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NotifyDetails {

    @JsonProperty("notifyEmail")
    private String notifyEmail;

    @JsonProperty("notifyTeam")
    private String notifyTeamName;

    public static NotifyDetails from(String notifyEmail, String notifyTeamName){
        return new NotifyDetails(notifyEmail, notifyTeamName);
    }
}
