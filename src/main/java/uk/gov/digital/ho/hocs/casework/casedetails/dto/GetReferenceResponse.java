package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;

@AllArgsConstructor
@Getter
public class GetReferenceResponse {

    @JsonProperty("type")
    private ReferenceType type;

    @JsonProperty("reference")
    private String reference;

    public static GetReferenceResponse from(ReferenceData referenceData) {
        return new GetReferenceResponse(
                referenceData.getReferenceType(),
                referenceData.getReference());

    }
}
