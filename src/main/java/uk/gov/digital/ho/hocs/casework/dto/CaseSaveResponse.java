package uk.gov.digital.ho.hocs.casework.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.rsh.RshCaseDetails;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CaseSaveResponse {

    private final String caseReference;

    private final String uuid;

    public static CaseSaveResponse from(RshCaseDetails caseDetails) {
        return new CaseSaveResponse(caseDetails.getCaseReference(), caseDetails.getUuid());
    }
}
