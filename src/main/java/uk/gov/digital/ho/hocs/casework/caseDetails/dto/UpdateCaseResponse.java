package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UpdateCaseResponse {

    private final String caseReference;

    private final UUID uuid;

    public static UpdateCaseResponse from(CaseData caseData) {
        return new UpdateCaseResponse(caseData.getReference(), caseData.getUuid());
    }
}
