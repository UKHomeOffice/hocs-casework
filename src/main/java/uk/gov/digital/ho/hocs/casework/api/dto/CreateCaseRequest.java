package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

@AllArgsConstructor
@Getter
public class CreateCaseRequest {

    @JsonProperty("type")
    private CaseDataType type;
}