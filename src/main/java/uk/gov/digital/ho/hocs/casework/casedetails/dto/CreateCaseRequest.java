package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CaseType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private CaseType type;

}
