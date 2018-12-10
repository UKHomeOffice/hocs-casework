package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

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
    private String type;

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("fullname")
    private String fullname;

    @JsonProperty("address")
    private AddressDto address;

    @JsonProperty("telephone")
    private String telephone;

    @JsonProperty("email")
    private String email;

    @JsonProperty("reference")
    private String reference;

    public static CorrespondentDto from(Correspondent correspondent) {
        return new CorrespondentDto(
                correspondent.getUuid(),
                correspondent.getCreated(),
                correspondent.getCorrespondentType(),
                correspondent.getCaseUUID(),
                correspondent.getFullName(),
                AddressDto.from(correspondent),
                correspondent.getTelephone(),
                correspondent.getEmail(),
                correspondent.getReference()
        );
    }
}
