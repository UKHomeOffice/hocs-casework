package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCaseResponse {

    private final String caseReference;

    private final UUID uuid;

    public static CreateCaseResponse from(CaseData caseData) {
        return new CreateCaseResponse(caseData.getReference(), caseData.getUuid());
    }
}
