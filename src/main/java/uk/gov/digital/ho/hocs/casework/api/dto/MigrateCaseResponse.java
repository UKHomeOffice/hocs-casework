package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class MigrateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    private final Map<String, String> caseDataMap;

}
