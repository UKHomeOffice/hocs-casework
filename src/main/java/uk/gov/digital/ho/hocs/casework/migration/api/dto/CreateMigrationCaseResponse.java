package uk.gov.digital.ho.hocs.casework.migration.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import uk.gov.digital.ho.hocs.casework.api.dto.CreateCaseResponse;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;
import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PUBLIC)
@Getter
@Setter
public class CreateMigrationCaseResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    @JsonProperty("stageId")
    private final UUID stageId;

    @JsonProperty("reference")
    private final String reference;

    @JsonProperty("data")
    private Map<String, String> data;

    public static CreateMigrationCaseResponse from(CaseData caseData, UUID stageId) {
        return new CreateMigrationCaseResponse(caseData.getUuid(), stageId, caseData.getReference(), caseData.getDataMap());
    }

}
