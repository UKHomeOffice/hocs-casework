package uk.gov.digital.ho.hocs.casework.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.casework.rsh.RshCaseDetails;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CaseSummaryResponse {

    private final String caseReference;

    private final String uuid;

    public static CaseSummaryResponse from(RshCaseDetails caseDetails) {
        return new CaseSummaryResponse(caseDetails.getCaseReference(), caseDetails.getUuid());
    }

}
