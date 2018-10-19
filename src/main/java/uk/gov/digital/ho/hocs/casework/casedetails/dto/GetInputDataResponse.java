package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetInputDataResponse {

    @JsonRawValue
    private String data;

    public static GetInputDataResponse from(InputData inputData) {
        return new GetInputDataResponse(inputData.getData());
    }
}