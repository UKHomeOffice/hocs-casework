package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("RECORD_INTEREST")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataExternalInterestInboundDto extends ActionDataDto {

    private String interestedPartyType;
    private String detailsOfInterest;

    public ActionDataExternalInterestInboundDto(UUID uuid,
                                                UUID caseTypeActionUuid,
                                                String caseSubtype,
                                                String caseTypeActionLabel,
                                                String interestedPartyType,
                                                String detailsOfInterest) {
        super(uuid, caseTypeActionUuid, caseSubtype, caseTypeActionLabel);

        this.interestedPartyType = interestedPartyType;
        this.detailsOfInterest = detailsOfInterest;
    }
}
