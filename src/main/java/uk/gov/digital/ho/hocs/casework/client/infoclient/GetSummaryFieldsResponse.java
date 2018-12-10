package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Set;

@Getter
class GetSummaryFieldsResponse {

    @JsonProperty("fields")
    private Set<String> fields;

}
