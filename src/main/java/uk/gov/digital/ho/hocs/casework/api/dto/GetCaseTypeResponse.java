package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetCaseTypeResponse {

    @JsonProperty("type")
    private String type;

    public static GetCaseTypeResponse from(String type) {
        return new GetCaseTypeResponse(type);
    }

}
