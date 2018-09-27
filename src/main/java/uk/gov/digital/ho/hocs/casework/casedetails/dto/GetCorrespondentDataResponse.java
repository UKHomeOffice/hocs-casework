package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetCorrespondentDataResponse {

    @JsonProperty("type")
    CorrespondentType type;

    @JsonProperty("fullname")
    String fullname;

    @JsonProperty("postcode")
    String postcode;

    @JsonProperty("address1")
    String address1;

    @JsonProperty("address2")
    String address2;

    @JsonProperty("address3")
    String address3;

    @JsonProperty("country")
    String country;

    @JsonProperty("telephone")
    String telephone;

    @JsonProperty("email")
    String email;

    @JsonProperty("reference")
    String reference;

    public static GetCorrespondentDataResponse from(CorrespondentData correspondentData)
    {
        return new GetCorrespondentDataResponse(
            CorrespondentType.APPLICANT,
            correspondentData.getFullName(),
            correspondentData.getPostcode(),
            correspondentData.getAddress1(),
            correspondentData.getAddress2(),
            correspondentData.getAddress3(),
            correspondentData.getCountry(),
            correspondentData.getTelephone(),
            correspondentData.getEmail(),
            ""
        );
    }
}
