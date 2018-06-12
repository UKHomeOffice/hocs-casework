package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateRshCaseRequest {

    @JsonProperty("sendEmailRequest")
    private SendEmailRequest sendEmailRequest = new SendEmailRequest();

    @JsonProperty("caseData")
    private Map<String, String> caseData = new HashMap<>();
}