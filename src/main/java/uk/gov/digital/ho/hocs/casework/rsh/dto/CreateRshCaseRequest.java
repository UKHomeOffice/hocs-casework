package uk.gov.digital.ho.hocs.casework.rsh.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.email.dto.SendEmailRequest;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreateRshCaseRequest {

    @JsonProperty("sendEmailRequest")
    private SendEmailRequest notifyRequest;

    @JsonProperty("caseData")
    private Map<String,Object> caseData;
}