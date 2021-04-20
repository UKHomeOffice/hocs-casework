package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Exemption;

import java.util.Set;
import java.util.stream.Collectors;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetExemptionsResponse {

    @JsonProperty("exemptions")
    Set<GetExemptionResponse> exemptions;

    public static GetExemptionsResponse from(Set<Exemption> exemptions) {
        Set<GetExemptionResponse> exemptionsResponse = exemptions
                .stream()
                .map(GetExemptionResponse::from)
                .collect(Collectors.toSet());

        return new GetExemptionsResponse(exemptionsResponse);
    }

}
