package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.*;

import java.time.LocalDate;
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
                                                String caseTypeActionLabel,
                                                String interestedPartyType,
                                                String detailsOfInterest) {
        super(uuid, caseTypeActionUuid, caseTypeActionLabel);

        this.interestedPartyType = interestedPartyType;
        this.detailsOfInterest = detailsOfInterest;
    }
}
