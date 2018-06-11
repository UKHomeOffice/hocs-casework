package uk.gov.digital.ho.hocs.casework.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class SendEmailRequest {

    @JsonProperty("email")
    private String email;

    @JsonProperty("teamName")
    private String teamName;

    public static String toJsonString(ObjectMapper objectMapper, SendEmailRequest sendEmailRequest) {
        String ret = "";
        try {
            ret = objectMapper.writeValueAsString(sendEmailRequest);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
