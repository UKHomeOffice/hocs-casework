package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentType;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CorrespondentDto {


    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private CorrespondentType type;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("fullname")
    private String fullname;

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

    @JsonProperty("reference")
    private String reference;

    public static CorrespondentDto from(Correspondent correspondent)
    {
        return new CorrespondentDto(
                correspondent.getUuid(),
                correspondent.getCreated(),
                correspondent.getCorrespondentType(),
                correspondent.getCaseUUID(),
                correspondent.getFullName(),
                correspondent.getPostcode(),
                correspondent.getAddress1(),
                correspondent.getAddress2(),
                correspondent.getAddress3(),
                correspondent.getCountry(),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference()
        );
    }
}
