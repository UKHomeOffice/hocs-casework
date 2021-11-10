package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@ToString(callSuper = true)
@JsonTypeName("INTEREST_OUT")
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataExternalInterestOutboundDto extends ActionDataDto {

    private String interestedPartyType;
    private Map<String, String> interestedPartyEntity;
    private String detailsOfInterest;
    private String note;

    public ActionDataExternalInterestOutboundDto(UUID uuid,
                                                 UUID caseTypeActionUuid,
                                                 String caseTypeActionLabel,
                                                 String interestedPartyType,
                                                 Map<String, String> interestedPartyEntity,
                                                 String detailsOfInterest) {
        super(uuid, caseTypeActionUuid, caseTypeActionLabel);

        this.interestedPartyType = interestedPartyType;
        this.interestedPartyEntity = interestedPartyEntity;
        this.detailsOfInterest = detailsOfInterest;
    }
}
