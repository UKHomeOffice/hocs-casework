package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class WithdrawCaseRequest {

    @JsonProperty("notes")
    private String notes;

    @JsonProperty("withdrawalDate")
    private String withdrawalDate;

}
