package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Getter;

import java.util.Set;


@Getter
class GetSummaryFieldsResponse {

    private Set<String> data;

}
