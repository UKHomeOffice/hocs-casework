package uk.gov.digital.ho.hocs.casework.rsh;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.email.SendEmailRequest;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
class RshCaseCreateRequest {

    @JsonProperty("sendEmailRequest")
    private SendEmailRequest notifyRequest;

    @JsonProperty("caseData")
    private Map<String,Object> caseData;
}
