package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.CaseData;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseResponse {

    private final String caseReference;

    public static CreateCaseResponse from(CaseData caseData) {
        return new CreateCaseResponse(caseData.getReference());
    }
}
