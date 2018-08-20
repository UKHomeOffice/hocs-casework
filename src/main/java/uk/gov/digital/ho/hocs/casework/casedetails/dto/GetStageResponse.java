package uk.gov.digital.ho.hocs.casework.casedetails.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.InputData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageData;

import java.util.UUID;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class GetStageResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    @JsonProperty("type")
    private String type;

    @JsonProperty("caseReference")
    private String caseReference;

    @JsonProperty("data")
    private String data;

    public static GetStageResponse from(StageData stageData) {

        String caseRef = null;
        String data = null;
        InputData inputData = stageData.getInputData();
        if (inputData != null) {
            caseRef = stageData.getCaseReference();
            data = inputData.getData();
        }
        return new GetStageResponse(stageData.getUuid(), stageData.getType(), caseRef, data);
    }
}