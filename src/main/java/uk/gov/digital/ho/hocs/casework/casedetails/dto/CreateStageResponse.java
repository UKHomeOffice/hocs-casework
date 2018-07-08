package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class CreateStageResponse {

    @JsonProperty("uuid")
    private final UUID uuid;

    public static CreateStageResponse from(StageData stageData) {
        return new CreateStageResponse(stageData.getUuid());
    }
}
