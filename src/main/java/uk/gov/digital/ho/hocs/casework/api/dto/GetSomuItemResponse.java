package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetSomuItemResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("case_uuid")
    private UUID caseUUID;

    @JsonProperty("somu_uuid")
    private UUID somuUUID;

    @JsonProperty("data")
    private String data;

}
