package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataDetails;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@Getter
public class CaseDataDetailsDto {
    @JsonProperty("type")
    private final String type;
    @JsonProperty("reference")
    private final String reference;
    @JsonProperty("fields")
    private final Map<String, List<CaseDataDetails.Fields>> fields;
    @JsonProperty("data")
    private final Map<String, String> data;
}
