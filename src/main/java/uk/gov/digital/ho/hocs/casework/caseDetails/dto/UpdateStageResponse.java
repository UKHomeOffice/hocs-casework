package uk.gov.digital.ho.hocs.casework.caseDetails.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.caseDetails.model.StageData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class UpdateStageResponse {

    private final UUID uuid;

    public static UpdateStageResponse from(StageData stageData) {
        return new UpdateStageResponse(stageData.getUuid());
    }
}