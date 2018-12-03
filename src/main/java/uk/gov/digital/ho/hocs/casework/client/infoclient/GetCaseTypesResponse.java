package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.Set;

@Getter
class GetCaseTypesResponse {

    @JsonProperty("caseTypes")
    private Set<CaseDataType> caseTypes;

}
