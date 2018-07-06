package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateStageResponse {

    private final UUID uuid;

    public static CreateStageResponse from(StageData stageData) {
        return new CreateStageResponse(stageData.getUuid());
    }
}