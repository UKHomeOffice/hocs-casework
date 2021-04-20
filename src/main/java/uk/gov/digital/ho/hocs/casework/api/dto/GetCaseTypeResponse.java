package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;


@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseTypeResponse {
    @JsonProperty("type")
    private String type;

    public static GetCaseTypeResponse from(CaseData caseData) {

        return new GetCaseTypeResponse(caseData.getType());
    }

}
