package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.Set;

@AllArgsConstructor
@Getter
public class GetCaseTypesResponse {

    @JsonProperty("caseTypes")
    private Set<CaseDataType> caseTypes;

}
