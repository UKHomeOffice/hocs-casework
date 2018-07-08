package uk.gov.digital.ho.hocs.casework.rsh.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SendEmailRequest {

    @JsonProperty("emailAddress")
    private String emailAddress = "";

    @JsonProperty("personalisation")
    private Map<String, String> personalisation = new HashMap<>();

    public SendEmailRequest(String emailAddress, Map<String, String> personalisation) {
        if (emailAddress != null) {
            this.emailAddress = emailAddress;
        }
        if (personalisation != null) {
            this.personalisation = personalisation;
        }
    }
}


