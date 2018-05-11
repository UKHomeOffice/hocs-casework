package uk.gov.digital.ho.hocs.casework.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.rsh.RshCaseDetails;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class SearchResponse {

    private String caseReference;

    private final String uuid;

    private String caseData;

    public static SearchResponse from(RshCaseDetails caseDetails) {
        return new SearchResponse(caseDetails.getCaseReference(), caseDetails.getUuid(), caseDetails.getCaseData());
    }
}
