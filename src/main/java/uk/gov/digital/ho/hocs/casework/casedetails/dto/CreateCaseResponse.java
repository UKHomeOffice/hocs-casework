package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("reference")
    private final String reference;

    public static CreateCaseResponse from(CaseData caseData) {
        return new CreateCaseResponse(caseData.getUuid(), caseData.getReference());
    }
}