package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseDataType;

@AllArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;
}