package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class CreateRshCaseRequest {

    @JsonProperty("sendEmailRequest")
    private SendRshEmailRequest sendEmailRequest;

    @JsonProperty("caseData")
    private Map<String, String> caseData = new HashMap<>();
}