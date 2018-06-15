package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateRshCaseRequest {

    @JsonProperty("sendEmailRequest")
    private SendRshEmailRequest sendEmailRequest = new SendRshEmailRequest();

    @JsonProperty("caseData")
    private Map<String, String> caseData = new HashMap<>();
}