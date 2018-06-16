package uk.gov.digital.ho.hocs.casework.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@AllArgsConstructor
@Getter
public class SendEmailRequest {

    @JsonProperty("emailAddress")
    private String emailAddress;

    @JsonProperty("personalisation")
    private Map<String, String> personalisation;

}


