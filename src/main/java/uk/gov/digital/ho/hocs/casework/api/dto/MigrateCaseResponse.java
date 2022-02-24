package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class MigrateCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("reference")
    private final String reference;

    public static MigrateCaseResponse from(CaseData caseData) {
        return new MigrateCaseResponse(caseData.getUuid(), caseData.getReference());
    }
}
