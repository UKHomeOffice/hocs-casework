package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseData;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetInputDataResponse {

    @JsonRawValue
    private String data;

    public static GetInputDataResponse from(CaseData caseData) {
        return new GetInputDataResponse(caseData.getData());
    }
}