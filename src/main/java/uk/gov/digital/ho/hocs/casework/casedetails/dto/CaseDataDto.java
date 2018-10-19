package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.time.LocalDateTime;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseDataDto {

    @JsonProperty("type")
    private String caseType;

    @JsonProperty("reference")
    private String reference;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;


    public static CaseDataDto from(CaseData caseData) {

        String caseRef = caseData.getReference();

        return new CaseDataDto(
                caseData.getTypeString(),
                caseRef,
                caseData.getUuid(),
                caseData.getCreated());
    }
}
