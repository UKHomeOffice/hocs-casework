package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCorrespondentWithPrimaryFlagResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("created")
    private LocalDateTime created;

    @JsonProperty("type")
    private String type;

    @JsonProperty("typeDisplayName")
    private String typeDisplayName;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("organisation")
    private String organisation;

    @JsonProperty("address")
    private AddressDto address;

    @JsonProperty("telephone")
    private String telephone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("externalKey")
    private String externalKey;

    @JsonProperty("isPrimary")
    private Boolean isPrimary;

    public static GetCorrespondentWithPrimaryFlagResponse from(CorrespondentWithPrimaryFlag correspondent) {
        return new GetCorrespondentWithPrimaryFlagResponse(
                correspondent.getUuid(),
                correspondent.getCreated(),
                correspondent.getCorrespondentType(),
                correspondent.getCorrespondentTypeName(),
                correspondent.getCaseUUID(),
                correspondent.getFullName(),
                correspondent.getOrganisation(),
                AddressDto.from(correspondent),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference(),
                correspondent.getExternalKey(),
                correspondent.getIsPrimary()
        );
    }

}
