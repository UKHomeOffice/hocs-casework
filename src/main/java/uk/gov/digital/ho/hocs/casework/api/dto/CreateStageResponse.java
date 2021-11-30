package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateStageResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    public static CreateStageResponse from(StageWithCaseData stage) {
        return new CreateStageResponse(stage.getUuid());
    }
}
