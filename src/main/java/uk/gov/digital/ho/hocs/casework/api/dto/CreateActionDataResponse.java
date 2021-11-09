package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateActionDataResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("case_uuid")
    private UUID caseUuid;

    @JsonProperty("reference")
    private String reference;
}
