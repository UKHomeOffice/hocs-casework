package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("reference")
    private final String reference;

    @JsonProperty("data")
    private Map<String, String> data;

    public static CreateCaseResponse from(CaseData caseData) {
        return new CreateCaseResponse(caseData.getUuid(), caseData.getReference(), caseData.getDataMap());
    }

}
