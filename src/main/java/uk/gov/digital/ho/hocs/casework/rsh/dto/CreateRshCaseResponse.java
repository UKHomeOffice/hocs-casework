package uk.gov.digital.ho.hocs.casework.rsh.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateRshCaseResponse {

    private final String caseReference;

    private final UUID uuid;

    public static CreateRshCaseResponse from(CaseData caseData) {
        return new CreateRshCaseResponse(caseData.getReference(), caseData.getUuid());
    }
}
