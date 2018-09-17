package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseTypeResponse {

    @JsonProperty("type")
    private String type;

    public static GetCaseTypeResponse from(CaseData caseData) {

        return new GetCaseTypeResponse(
                caseData.getTypeString());
    }
}
