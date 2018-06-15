package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SendRshEmailRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("teamName")
    private String teamName;
}
