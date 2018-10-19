package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;

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

    public static GetCorrespondentDataResponse from(Correspondent correspondent)
    {
        return new GetCorrespondentDataResponse(
            CorrespondentType.APPLICANT,
                correspondent.getFullName(),
                correspondent.getPostcode(),
                correspondent.getAddress1(),
                correspondent.getAddress2(),
                correspondent.getAddress3(),
                correspondent.getCountry(),
                correspondent.getTelephone(),
                correspondent.getEmail(),
            ""
        );
    }
}
