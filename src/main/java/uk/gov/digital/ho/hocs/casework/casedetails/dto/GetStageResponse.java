package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class GetStageResponse {

    @JsonProperty("type")
    private String type;

    @JsonProperty("data")
    private String data;

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("caseReference")
    private String caseReference;

    public static GetStageResponse from(StageData stageData) {
        return new GetStageResponse(stageData.getType().toString(), stageData.getData(), stageData.getUuid(), stageData.getCaseReference());
    }
}
