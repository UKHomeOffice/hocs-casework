package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;

@AllArgsConstructor
@Getter
public class CorrespondentDto {

    @JsonProperty("uuid")
    private String uuid;

    @JsonProperty("title")
    private String title;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("postcode")
    private String postcode;

    @JsonProperty("address1")
    private String address1;

    @JsonProperty("address2")
    private String address2;

    @JsonProperty("address3")
    private String address3;

    @JsonProperty("country")
    private String country;

    @JsonProperty("telephone")
    private String telephone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("type")
    private CorrespondentType type;

    public static CorrespondentDto from(CorrespondentData correspondentData) {
        return new CorrespondentDto(
                correspondentData.getUuid().toString(),
                correspondentData.getTitle(),
                correspondentData.getFirstName(),
                correspondentData.getSurname(),
                correspondentData.getPostcode(),
                correspondentData.getAddress1(),
                correspondentData.getAddress2() ,
                correspondentData.getAddress3() ,
                correspondentData.getCountry(),
                correspondentData.getTelephone(),
                correspondentData.getEmail(),
                correspondentData.getType());
    }

}
