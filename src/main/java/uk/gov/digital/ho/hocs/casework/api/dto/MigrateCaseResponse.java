package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class MigrateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    private final Map<String, String> caseDataMap;
}
