package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Reference;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;

@AllArgsConstructor
@Getter
public class GetReferenceResponse {

    @JsonProperty("type")
    private ReferenceType type;

    @JsonProperty("reference")
    private String reference;

    public static GetReferenceResponse from(Reference reference) {
        return new GetReferenceResponse(
                reference.getReferenceType(),
                reference.getReference());

    }
}
